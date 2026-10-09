package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prompter_settings")

class SettingsManager(private val context: Context) {

    private object PreferencesKeys {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val IS_MIRRORED = booleanPreferencesKey("is_mirrored")
        val TEXT_COLOR_HEX = stringPreferencesKey("text_color_hex")
        val TEXT_ALIGNMENT = stringPreferencesKey("text_alignment")
        val IS_BOLD = booleanPreferencesKey("is_bold")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val HORIZONTAL_PADDING = floatPreferencesKey("horizontal_padding")
        val OVERLAY_ALPHA = floatPreferencesKey("overlay_alpha")
        val COUNTDOWN_SECONDS = intPreferencesKey("countdown_seconds")
        val IS_CAMERA_ENABLED = booleanPreferencesKey("is_camera_enabled")
        val IS_FRONT_CAMERA = booleanPreferencesKey("is_front_camera")
        val SHOW_EYE_GUIDE = booleanPreferencesKey("show_eye_guide")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val DRAFT_TITLE = stringPreferencesKey("draft_title")
        val DRAFT_CONTENT = stringPreferencesKey("draft_content")
        val DRAFT_SCRIPT_ID = longPreferencesKey("draft_script_id")
    }

    val settingsFlow: Flow<PrompterSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
        PrompterSettings(
            fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 34f,
            scrollSpeed = preferences[PreferencesKeys.SCROLL_SPEED] ?: 35f,
            isMirrored = preferences[PreferencesKeys.IS_MIRRORED] ?: false,
            textColorHex = preferences[PreferencesKeys.TEXT_COLOR_HEX] ?: "#FFFFFF",
            textAlignment = preferences[PreferencesKeys.TEXT_ALIGNMENT] ?: "CENTER",
            isBold = preferences[PreferencesKeys.IS_BOLD] ?: true,
            lineSpacing = preferences[PreferencesKeys.LINE_SPACING] ?: 1.4f,
            horizontalPadding = preferences[PreferencesKeys.HORIZONTAL_PADDING] ?: 24f,
            overlayAlpha = preferences[PreferencesKeys.OVERLAY_ALPHA] ?: 0.60f,
            countdownSeconds = preferences[PreferencesKeys.COUNTDOWN_SECONDS] ?: 3,
            isCameraEnabled = preferences[PreferencesKeys.IS_CAMERA_ENABLED] ?: true,
            isFrontCamera = preferences[PreferencesKeys.IS_FRONT_CAMERA] ?: true,
            showEyeGuide = preferences[PreferencesKeys.SHOW_EYE_GUIDE] ?: true,
            keepScreenAwake = preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] ?: true,
            hasSeenOnboarding = preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false,
            draftTitle = preferences[PreferencesKeys.DRAFT_TITLE] ?: "",
            draftContent = preferences[PreferencesKeys.DRAFT_CONTENT] ?: "",
            draftScriptId = preferences[PreferencesKeys.DRAFT_SCRIPT_ID] ?: -1L
        )
    }

    suspend fun updateSettings(settings: PrompterSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = settings.fontSize
            preferences[PreferencesKeys.SCROLL_SPEED] = settings.scrollSpeed
            preferences[PreferencesKeys.IS_MIRRORED] = settings.isMirrored
            preferences[PreferencesKeys.TEXT_COLOR_HEX] = settings.textColorHex
            preferences[PreferencesKeys.TEXT_ALIGNMENT] = settings.textAlignment
            preferences[PreferencesKeys.IS_BOLD] = settings.isBold
            preferences[PreferencesKeys.LINE_SPACING] = settings.lineSpacing
            preferences[PreferencesKeys.HORIZONTAL_PADDING] = settings.horizontalPadding
            preferences[PreferencesKeys.OVERLAY_ALPHA] = settings.overlayAlpha
            preferences[PreferencesKeys.COUNTDOWN_SECONDS] = settings.countdownSeconds
            preferences[PreferencesKeys.IS_CAMERA_ENABLED] = settings.isCameraEnabled
            preferences[PreferencesKeys.IS_FRONT_CAMERA] = settings.isFrontCamera
            preferences[PreferencesKeys.SHOW_EYE_GUIDE] = settings.showEyeGuide
            preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] = settings.keepScreenAwake
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = settings.hasSeenOnboarding
        }
    }

    suspend fun saveDraft(title: String, content: String, scriptId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DRAFT_TITLE] = title
            preferences[PreferencesKeys.DRAFT_CONTENT] = content
            preferences[PreferencesKeys.DRAFT_SCRIPT_ID] = scriptId
        }
    }

    suspend fun setOnboardingSeen(seen: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = seen
        }
    }

    suspend fun updateSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCROLL_SPEED] = speed.coerceIn(5f, 100f)
        }
    }
}
