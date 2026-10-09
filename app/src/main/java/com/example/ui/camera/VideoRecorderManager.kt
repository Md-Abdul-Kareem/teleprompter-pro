package com.example.ui.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale

class VideoRecorderManager(private val context: Context) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null

    sealed class RecordState {
        object Idle : RecordState()
        object Initializing : RecordState()
        data class RecordingActive(val durationSeconds: Long) : RecordState()
        data class Paused(val durationSeconds: Long) : RecordState()
        data class Finalized(val uri: Uri?) : RecordState()
        data class Error(val message: String) : RecordState()
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        onCameraBound: (Boolean) -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val qualitySelector = QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD),
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )

                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
                onCameraBound(true)
            } catch (e: Exception) {
                Log.e("VideoRecorderManager", "Use case binding failed", e)
                onCameraBound(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording(
        hasAudioPermission: Boolean,
        onEvent: (RecordState) -> Unit
    ) {
        if (activeRecording != null) {
            return
        }

        val capture = videoCapture ?: run {
            onEvent(RecordState.Error("Camera recording not ready"))
            return
        }

        val name = "Teleprompter_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TeleprompterPro")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        var pendingRecording = capture.output
            .prepareRecording(context, mediaStoreOutputOptions)

        if (hasAudioPermission) {
            try {
                pendingRecording = pendingRecording.withAudioEnabled()
            } catch (e: SecurityException) {
                Log.w("VideoRecorderManager", "Audio permission missing for recording", e)
            }
        }

        try {
            activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        onEvent(RecordState.RecordingActive(0))
                    }
                    is VideoRecordEvent.Status -> {
                        val durationSec = recordEvent.recordingStats.recordedDurationNanos / 1_000_000_000L
                        onEvent(RecordState.RecordingActive(durationSec))
                    }
                    is VideoRecordEvent.Pause -> {
                        val durationSec = recordEvent.recordingStats.recordedDurationNanos / 1_000_000_000L
                        onEvent(RecordState.Paused(durationSec))
                    }
                    is VideoRecordEvent.Resume -> {
                        val durationSec = recordEvent.recordingStats.recordedDurationNanos / 1_000_000_000L
                        onEvent(RecordState.RecordingActive(durationSec))
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (recordEvent.hasError()) {
                            Log.e("VideoRecorderManager", "Recording error: ${recordEvent.error}")
                            onEvent(RecordState.Error("Recording error code: ${recordEvent.error}"))
                        } else {
                            val outputUri = recordEvent.outputResults.outputUri
                            onEvent(RecordState.Finalized(outputUri))
                        }
                        activeRecording = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VideoRecorderManager", "Failed to start recording", e)
            activeRecording = null
            onEvent(RecordState.Error(e.localizedMessage ?: "Failed to start recording"))
        }
    }

    fun pauseRecording() {
        activeRecording?.pause()
    }

    fun resumeRecording() {
        activeRecording?.resume()
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun isRecording(): Boolean {
        return activeRecording != null
    }

    fun unbind() {
        activeRecording?.stop()
        activeRecording = null
        cameraProvider?.unbindAll()
    }
}
