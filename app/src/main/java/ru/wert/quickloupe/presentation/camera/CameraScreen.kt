package ru.wert.quickloupe.presentation.camera

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Инициализируем камеру с lifecycleOwner
    LaunchedEffect(lifecycleOwner) {
        viewModel.initializeCamera(lifecycleOwner)
    }

    // Состояние из ViewModel
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()

    // Локальные состояния UI
    var frozenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    // Репозиторий для работы с камерой (временное решение до внедрения DI)
    val cameraRepository = remember(lifecycleOwner) {
        ru.wert.quickloupe.data.repository.CameraRepositoryImpl(context, lifecycleOwner)
    }

    // Разрешения
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

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
                    repository = cameraRepository,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Overlay с управлением
            CameraOverlay(
                state = cameraState,
                onZoomChanged = { zoom ->
                    viewModel.setZoom(zoom)
                },
                onFlashToggle = {
                    viewModel.toggleFlash()
                },
                onFreezeToggle = {
                    scope.launch {
                        if (!cameraState.isFrozen && !isCapturing) {
                            // Захватываем кадр
                            isCapturing = true
                            try {
                                delay(50)
                                val bitmap = cameraRepository.captureFrame()
                                if (bitmap != null) {
                                    frozenBitmap = bitmap
                                    viewModel.pauseCamera()
                                    viewModel.setFrozen(true)
                                } else {
                                    onError("Не удалось захватить изображение")
                                }
                            } finally {
                                isCapturing = false
                            }
                        } else if (cameraState.isFrozen) {
                            viewModel.resumeCamera()
                            frozenBitmap = null
                            viewModel.setFrozen(false)
                        }
                    }
                },
                onFilterToggle = {
                    val nextFilter = when (cameraState.currentFilter) {
                        ru.wert.quickloupe.domain.models.FilterType.NORMAL -> ru.wert.quickloupe.domain.models.FilterType.INVERTED
                        ru.wert.quickloupe.domain.models.FilterType.INVERTED -> ru.wert.quickloupe.domain.models.FilterType.GRAYSCALE
                        ru.wert.quickloupe.domain.models.FilterType.GRAYSCALE -> ru.wert.quickloupe.domain.models.FilterType.HIGH_CONTRAST
                        ru.wert.quickloupe.domain.models.FilterType.HIGH_CONTRAST -> ru.wert.quickloupe.domain.models.FilterType.NORMAL
                    }
                    viewModel.setFilter(nextFilter)
                },
                onBackPressed = onBackPressed,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PermissionRequiredScreen(
                permissionState = cameraPermissionState,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                onBackPressed = onBackPressed
            )
        }

        // Индикатор загрузки
        if (cameraState.isLoading || isCapturing) {
            LoadingIndicator()
        }

        // Сообщение об ошибке
        cameraState.error?.let { error ->
            ErrorMessage(
                message = error,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

@Composable
private fun CameraPreviewView(
    repository: ru.wert.quickloupe.data.repository.CameraRepositoryImpl,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            repository.getPreviewView() ?: repository.createPreviewView()
        },
        modifier = modifier,
        update = { previewView ->
            previewView.invalidate()
        }
    )
}

@Composable
private fun FrozenFrameView(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    Image(
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