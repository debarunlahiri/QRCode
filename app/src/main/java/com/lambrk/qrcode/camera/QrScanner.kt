package com.lambrk.qrcode.camera

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Full camera preview that decodes QR codes of any length (fully offline).
 * [onStreaming] tells the UI when frames are visible (to hide the loader),
 * [onTorchAvailable] whether the device has a flash unit. Pinch zooms (reported via [onZoomChanged]),
 * tap focuses ([onFocusTap] gets the tapped point so the UI can draw a focus ring).
 */
@Composable
fun QrScanner(
    modifier: Modifier = Modifier,
    torchOn: Boolean,
    onStreaming: (Boolean) -> Unit,
    onTorchAvailable: (Boolean) -> Unit,
    onZoomChanged: (Float) -> Unit = {},
    onFocusTap: (Offset) -> Unit = {},
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnResult = rememberUpdatedState(onResult)
    val currentOnStreaming = rememberUpdatedState(onStreaming)
    val currentOnTorch = rememberUpdatedState(onTorchAvailable)
    val currentOnZoom = rememberUpdatedState(onZoomChanged)
    val currentOnFocusTap = rememberUpdatedState(onFocusTap)

    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
    }
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        val streamObserver = Observer<PreviewView.StreamState> {
            currentOnStreaming.value(it == PreviewView.StreamState.STREAMING)
        }
        previewView.previewStreamState.observe(lifecycleOwner, streamObserver)

        providerFuture.addListener({
            val p = providerFuture.get()
            provider = p
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { proxy ->
                val media = proxy.image
                if (media == null) {
                    proxy.close()
                    return@setAnalyzer
                }
                val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener { codes ->
                        codes.firstNotNullOfOrNull { it.rawValue }?.let { text ->
                            previewView.post { currentOnResult.value(text) }
                        }
                    }
                    .addOnFailureListener { Log.w("QrScanner", "Scan failed", it) }
                    .addOnCompleteListener { proxy.close() }
            }
            try {
                p.unbindAll()
                val cam = p.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                )
                camera = cam
                currentOnTorch.value(cam.cameraInfo.hasFlashUnit())
            } catch (e: Exception) {
                Log.e("QrScanner", "Camera bind failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            previewView.previewStreamState.removeObserver(streamObserver)
            provider?.unbindAll()
            camera = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            executor.shutdown()
        }
    }

    LaunchedEffect(camera, torchOn) {
        camera?.takeIf { it.cameraInfo.hasFlashUnit() }?.cameraControl?.enableTorch(torchOn)
    }

    // Report zoom changes (pinch) to the UI.
    DisposableEffect(camera) {
        val cam = camera
        val observer = Observer<ZoomState> { currentOnZoom.value(it.zoomRatio) }
        cam?.cameraInfo?.zoomState?.observe(lifecycleOwner, observer)
        onDispose { cam?.cameraInfo?.zoomState?.removeObserver(observer) }
    }

    // Continuous autofocus is on by default; also lock focus + exposure on the centre at start.
    LaunchedEffect(camera) {
        val cam = camera ?: return@LaunchedEffect
        previewView.post {
            val point = previewView.meteringPointFactory.createPoint(previewView.width / 2f, previewView.height / 2f)
            runCatching { cam.cameraControl.startFocusAndMetering(FocusMeteringAction.Builder(point).build()) }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
            // Pinch to zoom in / out.
            .pointerInput(camera) {
                detectTransformGestures { _, _, zoom, _ ->
                    val cam = camera ?: return@detectTransformGestures
                    val state = cam.cameraInfo.zoomState.value ?: return@detectTransformGestures
                    val target = (state.zoomRatio * zoom).coerceIn(state.minZoomRatio, state.maxZoomRatio)
                    cam.cameraControl.setZoomRatio(target)
                }
            }
            // Tap to focus.
            .pointerInput(camera) {
                detectTapGestures { offset ->
                    val cam = camera ?: return@detectTapGestures
                    val point = previewView.meteringPointFactory.createPoint(offset.x, offset.y)
                    val action = FocusMeteringAction.Builder(point)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
                    runCatching { cam.cameraControl.startFocusAndMetering(action) }
                    currentOnFocusTap.value(offset)
                }
            }
    )
}
