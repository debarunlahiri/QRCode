package com.lambrk.qrcode.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lambrk.qrcode.camera.QrScanner
import com.lambrk.qrcode.data.remote.ApiConfig
import com.lambrk.qrcode.domain.model.ConnectionIssue
import com.lambrk.qrcode.domain.model.ScanStep
import com.lambrk.qrcode.ui.components.ConnectionDialog
import com.lambrk.qrcode.ui.scan.components.CameraChrome
import com.lambrk.qrcode.ui.scan.components.EquipmentDoneScreen
import com.lambrk.qrcode.ui.scan.components.FocusRing
import com.lambrk.qrcode.ui.scan.components.ZoomChip
import com.lambrk.qrcode.ui.scan.components.LoadingOverlay
import com.lambrk.qrcode.ui.scan.components.PermissionScreen
import com.lambrk.qrcode.ui.scan.components.ResultScreen
import com.lambrk.qrcode.ui.scan.components.ScanOverlay

@Composable
fun ScanScreen(viewModel: ScanViewModel = viewModel(factory = ScanViewModel.Factory)) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    var cameraGranted by remember { mutableStateOf(isGranted(Manifest.permission.CAMERA)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { cameraGranted = isGranted(Manifest.permission.CAMERA) }

    // On start: ask for camera, and (development only) for notifications on Android 13+ so Chucker can post its notification.
    LaunchedEffect(Unit) {
        val missing = buildList {
            if (!isGranted(Manifest.permission.CAMERA)) add(Manifest.permission.CAMERA)
            if (ApiConfig.isDevelopment && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !isGranted(Manifest.permission.POST_NOTIFICATIONS)
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
    }

    var lastAccepted by remember { mutableStateOf(0L) }
    var cameraReady by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    var hasTorch by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableStateOf(1f) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var focusTick by remember { mutableStateOf(0) }
    LaunchedEffect(focusTick) {
        if (focusTick > 0) {
            delay(900)
            focusPoint = null
        }
    }

    Box(Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        when {
            state.step == ScanStep.DONE -> ResultScreen(state, viewModel::reset)
            state.step == ScanStep.EQUIPMENT_DONE ->
                EquipmentDoneScreen(state, viewModel::startMaterialScan, viewModel::backToEquipment)

            !cameraGranted -> PermissionScreen {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }

            else -> {
                DisposableEffect(Unit) { onDispose { cameraReady = false } }
                QrScanner(
                    modifier = Modifier.fillMaxSize(),
                    torchOn = torchOn,
                    onStreaming = { cameraReady = it },
                    onTorchAvailable = { hasTorch = it },
                    onZoomChanged = { zoomRatio = it },
                    onFocusTap = { focusPoint = it; focusTick++ }
                ) { text ->
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastAccepted > 1500 && viewModel.onScanned(text)) {
                        lastAccepted = now
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                ScanOverlay(active = cameraReady && !state.verifying)
                focusPoint?.let { FocusRing(it) }
                if (zoomRatio > 1.05f) ZoomChip(
                    zoomRatio,
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = 112.dp)
                )
                CameraChrome(state, torchOn, hasTorch, { torchOn = it }, viewModel::backToEquipment)
                if (!cameraReady) LoadingOverlay("Starting camera", Color.Black)
                if (state.verifying) LoadingOverlay("Verifying", Color.Black.copy(alpha = 0.7f))
            }
        }
    }

    if (state.sameQrDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSameQrDialog,
            title = { Text("Same QR code scanned") },
            text = {
                Text("This is the equipment QR code. Please scan the material QR code instead.")
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissSameQrDialog) { Text("OK") }
            }
        )
    }
}
