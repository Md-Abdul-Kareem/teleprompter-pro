package com.example.data.preferences

data class PrompterSettings(
    val fontSize: Float = 34f,
    val scrollSpeed: Float = 35f,
    val isMirrored: Boolean = false,
    val textColorHex: String = "#FFFFFF",
    val textAlignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
    val isBold: Boolean = true,
    val lineSpacing: Float = 1.4f,
    val horizontalPadding: Float = 24f,
    val overlayAlpha: Float = 0.60f,
    val countdownSeconds: Int = 3,
    val isCameraEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val showEyeGuide: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val hasSeenOnboarding: Boolean = false,
    val draftTitle: String = "",
    val draftContent: String = "",
    val draftScriptId: Long = -1L
)
