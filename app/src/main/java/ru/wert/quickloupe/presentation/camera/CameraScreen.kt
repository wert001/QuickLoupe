package ru.wert.quickloupe.presentation.camera

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Состояние камеры
    val cameraController = remember { CameraController(context, lifecycleOwner) }
    var cameraState by remember { mutableStateOf(CameraState()) }
    var frozenBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Разрешения
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Инициализация камеры при первом запуске
    LaunchedEffect(Unit) {
        if (cameraPermissionState.status.isGranted) {
            initializeCamera(cameraController, onError)
        }
    }

    // Обновление состояния камеры
    LaunchedEffect(cameraController) {
        cameraController.state.collect { state ->
            cameraState = state
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            if (cameraState.isFrozen && frozenBitmap != null) {
                // Показываем замороженный кадр
                FrozenFrameView(
                    bitmap = frozenBitmap!!,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Показываем живой поток с камеры
                CameraPreviewView(
                    controller = cameraController,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Overlay с управлением
            CameraOverlay(
                state = cameraState,
                onZoomChanged = { zoom ->
                    scope.launch {
                        cameraController.setZoom(zoom)
                    }
                },
                onFlashToggle = {
                    scope.launch {
                        cameraController.toggleFlash()
                    }
                },
                onFreezeToggle = {
                    scope.launch {
                        if (!cameraState.isFrozen) {
                            frozenBitmap = cameraController.captureFrame()
                        }
                        cameraController.toggleFreeze()
                    }
                },
                onFilterToggle = {
                    // Переключение фильтров
                    val nextFilter = when (cameraState.currentFilter) {
                        FilterType.NORMAL -> FilterType.INVERTED
                        FilterType.INVERTED -> FilterType.GRAYSCALE
                        FilterType.GRAYSCALE -> FilterType.HIGH_CONTRAST
                        FilterType.HIGH_CONTRAST -> FilterType.NORMAL
                    }
                    scope.launch {
                        cameraController.setFilter(nextFilter)
                    }
                },
                onBackPressed = onBackPressed,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Экран запроса разрешений
            PermissionRequiredScreen(
                permissionState = cameraPermissionState,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                onBackPressed = onBackPressed
            )
        }

        // Индикатор загрузки
        if (cameraState.isLoading) {
            LoadingIndicator()
        }

        // Сообщение об ошибке
        cameraState.error?.let { error ->
            ErrorMessage(
                message = error,
                onDismiss = { cameraController.clearError() }
            )
        }
    }
}

@Composable
private fun CameraPreviewView(
    controller: CameraController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            controller.createPreviewView(ctx)
        },
        modifier = modifier,
        update = { previewView ->
            // Обновление previewView при необходимости
        }
    )
}

@Composable
private fun FrozenFrameView(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Frozen frame",
        modifier = modifier,
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ошибка камеры") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private suspend fun initializeCamera(
    controller: CameraController,
    onError: (String) -> Unit
) {
    try {
        controller.initialize()
    } catch (e: Exception) {
        onError("Не удалось инициализировать камеру: ${e.message}")
    }
}