package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TeleprompterApp
import com.example.data.local.ScriptEntity
import com.example.data.preferences.PrompterSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeleprompterViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TeleprompterApp
    private val repository = app.repository
    private val settingsManager = app.settingsManager

    val settings: StateFlow<PrompterSettings> = settingsManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PrompterSettings()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val allScripts: StateFlow<List<ScriptEntity>> = combine(
        repository.allScripts,
        _searchQuery
    ) { scripts, query ->
        if (query.isBlank()) {
            scripts
        } else {
            scripts.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Active Script Editor State
    private val _currentScriptId = MutableStateFlow<Long?>(null)
    val currentScriptId = _currentScriptId.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle = _currentTitle.asStateFlow()

    private val _currentContent = MutableStateFlow("")
    val currentContent = _currentContent.asStateFlow()

    // Teleprompter Playback / Studio State
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isCountdownActive = MutableStateFlow(false)
    val isCountdownActive = _isCountdownActive.asStateFlow()

    private val _countdownRemaining = MutableStateFlow(3)
    val countdownRemaining = _countdownRemaining.asStateFlow()

    private val _areControlsVisible = MutableStateFlow(true)
    val areControlsVisible = _areControlsVisible.asStateFlow()

    private val _scrollProgress = MutableStateFlow(0f)
    val scrollProgress = _scrollProgress.asStateFlow()

    // Video Recording State
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _isRecordingPaused = MutableStateFlow(false)
    val isRecordingPaused = _isRecordingPaused.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0L)
    val recordingDurationSeconds = _recordingDurationSeconds.asStateFlow()

    private val _savedVideoUri = MutableStateFlow<Uri?>(null)
    val savedVideoUri = _savedVideoUri.asStateFlow()

    private val _recordingErrorMessage = MutableStateFlow<String?>(null)
    val recordingErrorMessage = _recordingErrorMessage.asStateFlow()

    // Autosave Debounce Job
    private var autosaveJob: Job? = null
    private var controlsHideJob: Job? = null
    private var countdownJob: Job? = null

    init {
        // Restore Draft if exists (one-shot initialization)
        viewModelScope.launch {
            val currentSettings = settingsManager.settingsFlow.first()
            if (currentSettings.draftContent.isNotBlank() || currentSettings.draftTitle.isNotBlank()) {
                _currentTitle.value = currentSettings.draftTitle
                _currentContent.value = currentSettings.draftContent
                _currentScriptId.value = if (currentSettings.draftScriptId > 0) currentSettings.draftScriptId else null
            } else {
                _currentTitle.value = "Welcome to Teleprompter Pro"
                _currentContent.value = """Welcome to Teleprompter Pro!

This professional teleprompter is engineered for content creators, keynote speakers, journalists, and educators.

Key Features & Controls:
1. Camera Background: Record video directly to your phone's Gallery while reading.
2. Mirror Mode: Flips text horizontally when using physical teleprompter glass.
3. Smooth Auto-Scroll: Calibrate speed dynamically or tap on screen to pause.
4. Gesture Controls: Swipe anywhere on screen to manually scrub text.
5. Eye Guide Line: Keep perfect eye contact with your audience.

Tap the Start Prompter button below to begin reading!"""
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _currentTitle.value = newTitle
        triggerAutosave()
    }

    fun updateContent(newContent: String) {
        _currentContent.value = newContent
        triggerAutosave()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun triggerAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(1500) // 1.5s debounce
            settingsManager.saveDraft(
                title = _currentTitle.value,
                content = _currentContent.value,
                scriptId = _currentScriptId.value ?: -1L
            )
        }
    }

    fun loadScript(script: ScriptEntity) {
        _currentScriptId.value = script.id
        _currentTitle.value = script.title
        _currentContent.value = script.content
        viewModelScope.launch {
            settingsManager.saveDraft(script.title, script.content, script.id)
            settingsManager.updateSpeed(script.scrollSpeed)
        }
    }

    fun createNewScript() {
        _currentScriptId.value = null
        _currentTitle.value = "New Script"
        _currentContent.value = ""
        viewModelScope.launch {
            settingsManager.saveDraft("New Script", "", -1L)
        }
    }

    fun saveCurrentScript(onSaved: ((Long) -> Unit)? = null) {
        val title = _currentTitle.value.trim().ifEmpty { "Untitled Script" }
        val content = _currentContent.value
        val words = calculateWordCount(content)
        val currentSpeed = settings.value.scrollSpeed
        val currentFontSize = settings.value.fontSize
        val currentMirrored = settings.value.isMirrored

        viewModelScope.launch {
            val id = _currentScriptId.value
            if (id != null && id > 0) {
                val existing = repository.getScriptById(id)
                val updated = (existing ?: ScriptEntity(id = id, title = title, content = content)).copy(
                    title = title,
                    content = content,
                    wordCount = words,
                    updatedAt = System.currentTimeMillis(),
                    scrollSpeed = currentSpeed,
                    fontSize = currentFontSize,
                    isMirrored = currentMirrored
                )
                repository.updateScript(updated)
                onSaved?.invoke(id)
            } else {
                val newEntity = ScriptEntity(
                    title = title,
                    content = content,
                    wordCount = words,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    scrollSpeed = currentSpeed,
                    fontSize = currentFontSize,
                    isMirrored = currentMirrored
                )
                val newId = repository.insertScript(newEntity)
                _currentScriptId.value = newId
                settingsManager.saveDraft(title, content, newId)
                onSaved?.invoke(newId)
            }
        }
    }

    fun renameScript(script: ScriptEntity, newTitle: String) {
        val cleanTitle = newTitle.trim().ifEmpty { script.title }
        viewModelScope.launch {
            val updated = script.copy(title = cleanTitle, updatedAt = System.currentTimeMillis())
            repository.updateScript(updated)
            if (_currentScriptId.value == script.id) {
                _currentTitle.value = cleanTitle
                settingsManager.saveDraft(cleanTitle, _currentContent.value, script.id)
            }
        }
    }

    fun deleteScript(script: ScriptEntity) {
        viewModelScope.launch {
            repository.deleteScript(script)
            if (_currentScriptId.value == script.id) {
                createNewScript()
            }
        }
    }

    fun duplicateScript(script: ScriptEntity) {
        viewModelScope.launch {
            repository.duplicateScript(script)
        }
    }

    fun updateSettings(newSettings: PrompterSettings) {
        viewModelScope.launch {
            settingsManager.updateSettings(newSettings)
        }
    }

    fun nudgeSpeed(delta: Float) {
        val current = settings.value.scrollSpeed
        val updated = (current + delta).coerceIn(5f, 100f)
        viewModelScope.launch {
            settingsManager.updateSpeed(updated)
        }
    }

    fun toggleMirror() {
        val current = settings.value
        updateSettings(current.copy(isMirrored = !current.isMirrored))
    }

    fun toggleCamera() {
        val current = settings.value
        updateSettings(current.copy(isCameraEnabled = !current.isCameraEnabled))
    }

    fun toggleLens() {
        val current = settings.value
        updateSettings(current.copy(isFrontCamera = !current.isFrontCamera))
    }

    fun toggleEyeGuide() {
        val current = settings.value
        updateSettings(current.copy(showEyeGuide = !current.showEyeGuide))
    }

    // Teleprompter Playback
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pauseScrolling()
        } else {
            startScrollingWithCountdown()
        }
    }

    fun startScrollingWithCountdown() {
        val countdown = settings.value.countdownSeconds
        if (countdown > 0 && !_isPlaying.value) {
            _isCountdownActive.value = true
            _countdownRemaining.value = countdown
            countdownJob?.cancel()
            countdownJob = viewModelScope.launch {
                for (i in countdown downTo 1) {
                    _countdownRemaining.value = i
                    delay(900)
                }
                _countdownRemaining.value = 0 // Displays "GO!"
                delay(500)
                _isCountdownActive.value = false
                _isPlaying.value = true
                scheduleControlsAutoHide()
            }
        } else {
            _isCountdownActive.value = false
            _isPlaying.value = true
            scheduleControlsAutoHide()
        }
    }

    fun skipCountdown() {
        countdownJob?.cancel()
        _isCountdownActive.value = false
        _isPlaying.value = true
        scheduleControlsAutoHide()
    }

    fun pauseScrolling() {
        countdownJob?.cancel()
        _isCountdownActive.value = false
        _isPlaying.value = false
        showControls()
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
        if (playing) {
            scheduleControlsAutoHide()
        } else {
            showControls()
        }
    }

    fun updateScrollProgress(progress: Float) {
        _scrollProgress.value = progress.coerceIn(0f, 1f)
    }

    // Control Bar Visibility Auto-Hide
    fun toggleControlsVisibility() {
        if (_areControlsVisible.value) {
            _areControlsVisible.value = false
            controlsHideJob?.cancel()
        } else {
            showControls()
        }
    }

    fun showControls() {
        _areControlsVisible.value = true
        scheduleControlsAutoHide()
    }

    fun scheduleControlsAutoHide() {
        controlsHideJob?.cancel()
        if (_isPlaying.value) {
            controlsHideJob = viewModelScope.launch {
                delay(4000)
                if (_isPlaying.value) {
                    _areControlsVisible.value = false
                }
            }
        }
    }

    // Recording State Handlers
    fun onRecordingStarted() {
        _isRecording.value = true
        _isRecordingPaused.value = false
        _recordingDurationSeconds.value = 0L
        _recordingErrorMessage.value = null
    }

    fun onRecordingPaused(duration: Long) {
        _isRecordingPaused.value = true
        _recordingDurationSeconds.value = duration
    }

    fun onRecordingResumed(duration: Long) {
        _isRecordingPaused.value = false
        _recordingDurationSeconds.value = duration
    }

    fun onRecordingDurationUpdated(duration: Long) {
        _recordingDurationSeconds.value = duration
    }

    fun onRecordingFinalized(uri: Uri?) {
        _isRecording.value = false
        _isRecordingPaused.value = false
        _savedVideoUri.value = uri
    }

    fun onRecordingError(message: String) {
        _isRecording.value = false
        _isRecordingPaused.value = false
        _recordingErrorMessage.value = message
    }

    fun dismissSavedVideoDialog() {
        _savedVideoUri.value = null
    }

    fun dismissRecordingError() {
        _recordingErrorMessage.value = null
    }

    fun markOnboardingCompleted() {
        viewModelScope.launch {
            settingsManager.setOnboardingSeen(true)
        }
    }

    // Insert Script Templates
    fun loadTemplate(templateType: String) {
        when (templateType) {
            "SPEECH" -> {
                _currentTitle.value = "Keynote Presentation"
                _currentContent.value = """Good morning everyone, and thank you for being here today.

Today, I want to talk to you about a fundamental shift in how we communicate ideas.

When we share a story, our audience doesn't just listen to our words — they look into our eyes for conviction, clarity, and trust.

Over the past year, our team has dedicated hundreds of hours to mastering this craft.

Here is the three-part framework we discovered:
First: Clarity of thought.
Second: Relentless simplicity.
Third: Authentic delivery.

Thank you, and I look forward to your questions."""
            }
            "YOUTUBE" -> {
                _currentTitle.value = "YouTube Video Hook & Intro"
                _currentContent.value = """Stop scrolling! If you've been struggling to create professional videos by yourself, this is the video you cannot miss.

In this breakdown, I'm sharing 5 pro tips that will immediately level up your video production quality.

Tip number 1: Keep your prompter speed at your natural conversational pace.
Tip number 2: Maintain constant eye contact with the camera aperture.
Tip number 3: Break your script into concise bullet points.

Don't forget to hit the like button and subscribe for weekly content creation tutorials!"""
            }
            "PODCAST" -> {
                _currentTitle.value = "Podcast Episode Introduction"
                _currentContent.value = """Welcome back to another episode of The Modern Creator Podcast!

I am your host, and today we have a very special guest joining us to discuss the future of digital storytelling.

Before we jump in, a quick reminder that all resources mentioned in today's episode will be linked in the description below.

Sit back, relax, and let's get into today's conversation."""
            }
        }
        triggerAutosave()
    }

    companion object {
        fun calculateWordCount(text: String): Int {
            if (text.isBlank()) return 0
            return text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
        }

        fun calculateReadingTime(wordCount: Int, wpm: Int = 130): String {
            if (wordCount == 0) return "0s"
            val totalSeconds = ((wordCount.toDouble() / wpm) * 60).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return if (minutes > 0) {
                "${minutes}m ${seconds}s"
            } else {
                "${seconds}s"
            }
        }
    }
}
