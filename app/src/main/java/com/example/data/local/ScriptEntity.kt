package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val scrollSpeed: Float = 35f,
    val fontSize: Float = 32f,
    val textColorHex: String = "#FFFFFF",
    val isMirrored: Boolean = false,
    val isBold: Boolean = true,
    val textAlignment: String = "CENTER",
    val lineSpacing: Float = 1.4f,
    val horizontalPadding: Float = 24f,
    val overlayAlpha: Float = 0.65f
)
