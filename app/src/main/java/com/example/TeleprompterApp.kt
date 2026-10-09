package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preferences.SettingsManager
import com.example.data.repository.ScriptRepository

class TeleprompterApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ScriptRepository(database.scriptDao()) }
    val settingsManager by lazy { SettingsManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: TeleprompterApp
            private set
    }
}
