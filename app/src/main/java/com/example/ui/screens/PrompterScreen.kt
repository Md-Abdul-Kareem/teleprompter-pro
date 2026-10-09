package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.camera.CameraPreview
import com.example.ui.camera.VideoRecorderManager
import com.example.ui.components.CountdownOverlay
import com.example.ui.components.EyeGuideLine
import com.example.ui.components.OnboardingTutorialDialog
import com.example.ui.components.PrompterSettingsSheet
import com.example.ui.components.RecordingExitConfirmDialog
import com.example.ui.components.VideoSavedDialog
import com.example.ui.components.bounceClick
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioRecordingRed
import com.example.ui.theme.StudioSurface
import com.example.ui.viewmodel.TeleprompterViewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun PrompterScreen(
    viewModel: TeleprompterViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val title by viewModel.currentTitle.collectAsStateWithLifecycle()
    val content by viewModel.currentContent.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isCountdownActive by viewModel.isCountdownActive.collectAsStateWithLifecycle()
    val countdownRemaining by viewModel.countdownRemaining.collectAsStateWithLifecycle()
    val areControlsVisible by viewModel.areControlsVisible.collectAsStateWithLifecycle()

    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDurationSeconds.collectAsStateWithLifecycle()
    val savedVideoUri by viewModel.savedVideoUri.collectAsStateWithLifecycle()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(!settings.hasSeenOnboarding) }

    // Scroll state & smooth animation tracker
    val scrollState = rememberScrollState()
    var rawScrollY by remember { mutableFloatStateOf(0f) }

    // Camera & VideoRecorder instances
    val videoRecorderManager = remember { VideoRecorderManager(context) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (!hasCameraPermission) {
            Toast.makeText(context, "Running in studio mode without camera", Toast.LENGTH_SHORT).show()
        }
    }

    // Keep Screen Awake
    DisposableEffect(settings.keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Unbind camera only when PrompterScreen is completely closed
    DisposableEffect(Unit) {
        onDispose {
            videoRecorderManager.unbind()
        }
    }

    // Request permissions on entry if camera mode is enabled
    LaunchedEffect(settings.isCameraEnabled) {
        if (settings.isCameraEnabled && (!hasCameraPermission || !hasAudioPermission)) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    // Handle Hardware Back Button
    BackHandler {
        if (isRecording) {
            showExitConfirmDialog = true
        } else {
            viewModel.pauseScrolling()
            onNavigateBack()
        }
    }

    // Frame-Driven High Performance Smooth Scroll Loop (Zero GC overhead)
    LaunchedEffect(isPlaying, settings.scrollSpeed) {
        if (isPlaying) {
            var lastFrameTimeNanos = 0L
            while (isActive && isPlaying) {
                val frameTimeNanos = withFrameNanos { it }
                if (lastFrameTimeNanos != 0L) {
                    val deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    val speedPixelsPerSec = settings.scrollSpeed * 2.5f
                    val deltaPixels = speedPixelsPerSec * deltaSeconds
                    rawScrollY += deltaPixels

                    val maxScroll = scrollState.maxValue.toFloat()
                    if (maxScroll > 0) {
                        if (rawScrollY >= maxScroll) {
                            rawScrollY = maxScroll
                            viewModel.pauseScrolling()
                        }
                        scrollState.scrollTo(rawScrollY.toInt())
                        viewModel.updateScrollProgress(rawScrollY / maxScroll)
                    }
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    // Pause auto-scroll automatically if user touches to manually scrub
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress && isPlaying) {
            viewModel.pauseScrolling()
        }
    }

    // Sync rawScrollY during manual touch scroll
    LaunchedEffect(scrollState.value) {
        if (!isPlaying) {
            rawScrollY = scrollState.value.toFloat()
            val maxScroll = scrollState.maxValue.toFloat()
            if (maxScroll > 0) {
                viewModel.updateScrollProgress(rawScrollY / maxScroll)
            }
        }
    }

    // Calm breathing pulse animation for recording indicator
    val recPulseTransition = rememberInfiniteTransition(label = "rec_pulse")
    val recDotAlpha by recPulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_dot_alpha"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        viewModel.toggleControlsVisibility()
                    }
                )
            }
    ) {
        val screenHeight = maxHeight

        // 1. Camera Background Layer (Behind Text)
        if (settings.isCameraEnabled && hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                videoRecorderManager = videoRecorderManager,
                isFrontCamera = settings.isFrontCamera
            )
        }

        // 2. Dark Overlay Layer for Text Readability
        val overlayDarkness = if (settings.isCameraEnabled && hasCameraPermission) {
            settings.overlayAlpha
        } else {
            1.0f // Pure OLED black studio background when camera is disabled
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayDarkness))
        )

        // 3. Eye Guide Level Highlight Overlay
        if (settings.showEyeGuide) {
            EyeGuideLine(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 160.dp)
            )
        }

        // 4. Scrolling Text Layer (Mirrorable)
        val textColor = try {
            Color(android.graphics.Color.parseColor(settings.textColorHex))
        } catch (e: Exception) {
            Color.White
        }

        val textAlign = when (settings.textAlignment) {
            "LEFT" -> TextAlign.Left
            "RIGHT" -> TextAlign.Right
            else -> TextAlign.Center
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = if (settings.isMirrored) -1f else 1f // Mirror transform on text only
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = settings.horizontalPadding.dp),
                horizontalAlignment = when (settings.textAlignment) {
                    "LEFT" -> Alignment.Start
                    "RIGHT" -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ) {
                // Top Spacer so script starts near the eye guide line
                Spacer(modifier = Modifier.height(180.dp))

                // Script Content
                Text(
                    text = content.ifBlank { "No script content. Return to the editor to write your script." },
                    color = textColor,
                    fontSize = settings.fontSize.sp,
                    fontWeight = if (settings.isBold) FontWeight.Bold else FontWeight.Normal,
                    textAlign = textAlign,
                    lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Bottom Spacer so script can fully scroll past the top
                Spacer(modifier = Modifier.height(screenHeight - 100.dp))
            }
        }

        // 5. Reading Progress Bar (Top)
        val progress by viewModel.scrollProgress.collectAsStateWithLifecycle()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.TopCenter),
            color = StudioCyan,
            trackColor = Color(0x33000000)
        )

        // 6. Persistent Exit Button (Always accessible top left, low opacity when hidden)
        IconButton(
            onClick = {
                if (isRecording) {
                    showExitConfirmDialog = true
                } else {
                    viewModel.pauseScrolling()
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 10.dp, start = 12.dp)
                .size(44.dp)
                .align(Alignment.TopStart)
                .background(
                    if (areControlsVisible) StudioSurface.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.35f),
                    CircleShape
                )
                .bounceClick {
                    if (isRecording) {
                        showExitConfirmDialog = true
                    } else {
                        viewModel.pauseScrolling()
                        onNavigateBack()
                    }
                }
                .testTag("prompter_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Exit prompter",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // 7. Top Control Bar (Camera, Mirror, Settings, REC Timer)
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { -it },
            exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(44.dp)) // Space for back button

                // Center Title / Recording Indicator
                if (isRecording) {
                    val minutes = recordingDuration / 60
                    val seconds = recordingDuration % 60
                    val formattedTime = String.format("%02d:%02d", minutes, seconds)

                    Row(
                        modifier = Modifier
                            .background(StudioRecordingRed.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = recDotAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REC $formattedTime",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = title.ifBlank { "Teleprompter" },
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                // Top Right Quick Tools
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Mirror Mode Quick Toggle
                    IconButton(
                        onClick = { viewModel.toggleMirror() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (settings.isMirrored) StudioCyan.copy(alpha = 0.35f) else StudioSurface.copy(alpha = 0.8f), CircleShape)
                            .bounceClick { viewModel.toggleMirror() }
                            .testTag("toggle_mirror_button")
                    ) {
                        Icon(
                            Icons.Default.Flip,
                            contentDescription = "Mirror text",
                            tint = if (settings.isMirrored) StudioCyan else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Camera Switch (Front / Back)
                    if (settings.isCameraEnabled && hasCameraPermission) {
                        IconButton(
                            onClick = { viewModel.toggleLens() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(StudioSurface.copy(alpha = 0.8f), CircleShape)
                                .bounceClick { viewModel.toggleLens() }
                                .testTag("toggle_camera_lens_button")
                        ) {
                            Icon(
                                Icons.Default.FlipCameraAndroid,
                                contentDescription = "Switch Camera",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = {
                            viewModel.pauseScrolling()
                            showSettingsSheet = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(StudioSurface.copy(alpha = 0.8f), CircleShape)
                            .bounceClick {
                                viewModel.pauseScrolling()
                                showSettingsSheet = true
                            }
                            .testTag("prompter_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Prompter Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 8. Speed Nudge Floating Pill
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(StudioSurface.copy(alpha = 0.85f))
                    .border(1.dp, StudioBorder, RoundedCornerShape(24.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.nudgeSpeed(+5f) },
                    modifier = Modifier.size(38.dp).bounceClick { viewModel.nudgeSpeed(+5f) }.testTag("speed_up_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Speed up", tint = StudioCyan)
                }

                Text(
                    text = "${settings.scrollSpeed.toInt()}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.nudgeSpeed(-5f) },
                    modifier = Modifier.size(38.dp).bounceClick { viewModel.nudgeSpeed(-5f) }.testTag("speed_down_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Speed down", tint = StudioCyan)
                }
            }
        }

        // 9. Bottom Master Studio Controls (Play/Pause, Record, Reset)
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it },
            exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = StudioSurface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind / Reset to Top
                        IconButton(
                            onClick = {
                                viewModel.pauseScrolling()
                                rawScrollY = 0f
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(StudioCard, CircleShape)
                                .bounceClick {
                                    viewModel.pauseScrolling()
                                    rawScrollY = 0f
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(0)
                                    }
                                }
                                .testTag("reset_scroll_button")
                        ) {
                            Icon(
                                Icons.Default.Replay,
                                contentDescription = "Restart scroll",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Play / Pause Floating Master Button with Tactile Spring Bounce
                        FloatingActionButton(
                            onClick = { viewModel.togglePlayPause() },
                            containerColor = StudioBlue,
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(68.dp)
                                .bounceClick { viewModel.togglePlayPause() }
                                .testTag("prompter_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(36.dp),
                                tint = Color.Black
                            )
                        }

                        // Video Recording Action (When camera is on)
                        if (settings.isCameraEnabled && hasCameraPermission) {
                            if (!isRecording) {
                                // Start Video Recording Button
                                IconButton(
                                    onClick = {
                                        viewModel.onRecordingStarted()
                                        videoRecorderManager.startRecording(
                                            hasAudioPermission = hasAudioPermission
                                        ) { state ->
                                            when (state) {
                                                is VideoRecorderManager.RecordState.RecordingActive -> {
                                                    viewModel.onRecordingDurationUpdated(state.durationSeconds)
                                                }
                                                is VideoRecorderManager.RecordState.Paused -> {
                                                    viewModel.onRecordingPaused(state.durationSeconds)
                                                }
                                                is VideoRecorderManager.RecordState.Finalized -> {
                                                    viewModel.onRecordingFinalized(state.uri)
                                                }
                                                is VideoRecorderManager.RecordState.Error -> {
                                                    viewModel.onRecordingError(state.message)
                                                }
                                                else -> {}
                                            }
                                        }
                                        if (!isPlaying) {
                                            viewModel.startScrollingWithCountdown()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(StudioRecordingRed, CircleShape)
                                        .bounceClick {
                                            viewModel.onRecordingStarted()
                                            videoRecorderManager.startRecording(
                                                hasAudioPermission = hasAudioPermission
                                            ) { state ->
                                                when (state) {
                                                    is VideoRecorderManager.RecordState.RecordingActive -> {
                                                        viewModel.onRecordingDurationUpdated(state.durationSeconds)
                                                    }
                                                    is VideoRecorderManager.RecordState.Paused -> {
                                                        viewModel.onRecordingPaused(state.durationSeconds)
                                                    }
                                                    is VideoRecorderManager.RecordState.Finalized -> {
                                                        viewModel.onRecordingFinalized(state.uri)
                                                    }
                                                    is VideoRecorderManager.RecordState.Error -> {
                                                        viewModel.onRecordingError(state.message)
                                                    }
                                                    else -> {}
                                                }
                                            }
                                            if (!isPlaying) {
                                                viewModel.startScrollingWithCountdown()
                                            }
                                        }
                                        .testTag("start_recording_button")
                                ) {
                                    Icon(
                                        Icons.Default.FiberManualRecord,
                                        contentDescription = "Start Recording",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                // Stop Recording Button
                                IconButton(
                                    onClick = {
                                        videoRecorderManager.stopRecording()
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(StudioRecordingRed, CircleShape)
                                        .bounceClick { videoRecorderManager.stopRecording() }
                                        .testTag("stop_recording_button")
                                ) {
                                    Icon(
                                        Icons.Default.Stop,
                                        contentDescription = "Stop Recording",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else {
                            // Camera Toggle Button (when off)
                            IconButton(
                                onClick = {
                                    viewModel.toggleCamera()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(StudioCard, CircleShape)
                                    .bounceClick { viewModel.toggleCamera() }
                                    .testTag("enable_camera_button")
                            ) {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = "Enable Camera",
                                    tint = StudioCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 10. Countdown Overlay (3-2-1-GO!)
        if (isCountdownActive) {
            CountdownOverlay(
                remainingSeconds = countdownRemaining,
                onSkip = { viewModel.skipCountdown() }
            )
        }

        // 11. Saved Video Dialog (Saves to Phone Gallery)
        savedVideoUri?.let { uri ->
            VideoSavedDialog(
                videoUri = uri,
                onDismiss = { viewModel.dismissSavedVideoDialog() }
            )
        }

        // 12. Confirm Stop Recording & Exit Dialog
        if (showExitConfirmDialog) {
            RecordingExitConfirmDialog(
                onConfirm = {
                    videoRecorderManager.stopRecording()
                    viewModel.pauseScrolling()
                    showExitConfirmDialog = false
                    onNavigateBack()
                },
                onDismiss = { showExitConfirmDialog = false }
            )
        }

        // 13. First-Time Studio Tips Dialog
        if (showTutorialDialog) {
            OnboardingTutorialDialog(
                onDismiss = {
                    showTutorialDialog = false
                    viewModel.markOnboardingCompleted()
                }
            )
        }

        // 14. Prompter Settings Bottom Sheet
        if (showSettingsSheet) {
            PrompterSettingsSheet(
                settings = settings,
                onSettingsChanged = { viewModel.updateSettings(it) },
                onDismiss = { showSettingsSheet = false }
            )
        }
    }
}
