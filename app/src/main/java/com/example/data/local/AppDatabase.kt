package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ScriptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teleprompter_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    getDatabase(context).let { database ->
                        populateInitialScripts(database.scriptDao())
                    }
                }
            }
        }

        private suspend fun populateInitialScripts(dao: ScriptDao) {
            val defaultScript1 = ScriptEntity(
                title = "Welcome to Teleprompter Pro",
                content = """Welcome to Teleprompter Pro!

This professional teleprompter is engineered for content creators, keynote speakers, journalists, and educators.

Key Features & Controls:
1. Camera Background: Record 1080p video while reading your script directly in front of the lens.
2. Mirror Mode: Flips text horizontally when using physical beam splitter teleprompter glass.
3. Smooth Auto-Scroll: Calibrate speed dynamically or tap on screen to nudge.
4. Gesture Controls: Swipe anywhere on screen to manually scrub text.
5. Eye Guide Line: Keep perfect eye contact with your audience.

Tap the Play button below to test scrolling, or tap anywhere in the center to toggle full-screen distraction-free mode.

Good luck with your recording!""",
                wordCount = 104,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val defaultScript2 = ScriptEntity(
                title = "Video Intro & Hook Example",
                content = """Have you ever struggled to look natural while reading a script on camera?

In this video, I'm revealing the exact 3-step formula that top creators use to maintain flawless eye contact and speak with absolute confidence.

Step number one: Position your scrolling text as close to your camera lens as possible.
Step number two: Match your speaking pace to approximately 130 words per minute for maximum clarity.
Step number three: Rehearse once with the countdown timer before hitting record.

Make sure to like this video, subscribe to the channel, and let's dive right in!""",
                wordCount = 92,
                createdAt = System.currentTimeMillis() - 3600000L,
                updatedAt = System.currentTimeMillis() - 3600000L
            )

            dao.insertScript(defaultScript1)
            dao.insertScript(defaultScript2)
        }
    }
}
