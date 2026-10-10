package com.example.ui.camera

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    videoRecorderManager: VideoRecorderManager,
    isFrontCamera: Boolean = true,
    onCameraReady: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, isFrontCamera) {
        videoRecorderManager.bindCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            isFrontCamera = isFrontCamera,
            onCameraBound = onCameraReady
        )

        onDispose {
            // Keep provider state clean on dispose
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
