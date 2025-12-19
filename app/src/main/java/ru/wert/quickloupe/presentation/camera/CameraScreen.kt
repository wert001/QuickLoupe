package ru.wert.quickloupe.presentation.camera

import android.Manifest
import android.graphics.Bitmap
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    // Состояние из ViewModel
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()

    // Инициализируем камеру один раз
    LaunchedEffect(Unit) {
        viewModel.initializeCamera(lifecycleOwner)
    }

    // Локальные состояния UI
    var frozenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<android.view.View?>(null) }

    // Разрешения
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Получаем preview view из репозитория
    LaunchedEffect(cameraState.isInitialized) {
        if (cameraState.isInitialized) {
            val view = viewModel.getPreviewView()
            if (view != null && previewView == null) {
                previewView = view
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Проверка разрешений
            !cameraPermissionState.status.isGranted -> {
                PermissionRequiredScreen(
                    permissionState = cameraPermissionState,
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                    onBackPressed = onBackPressed
                )
            }

            // Загрузка
            cameraState.isLoading -> {
                LoadingIndicator()
            }

            // Камера готова
            cameraState.isInitialized -> {
                if (cameraState.isFrozen && frozenBitmap != null) {
                    // Показываем замороженный кадр
                    FrozenFrameView(
                        bitmap = frozenBitmap!!,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (previewView != null) {
                    // Показываем живой поток с камеры
                    CameraPreviewView(
                        previewView = previewView!!,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Показываем загрузку пока preview не готов
                    LoadingIndicator()
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
                                    delay(100) // Даем время для стабилизации
                                    val bitmap = viewModel.captureFrame()
                                    if (bitmap != null) {
                                        frozenBitmap = bitmap
                                        viewModel.pauseCamera() // Останавливаем камеру
                                        viewModel.setFrozen(true)
                                    } else {
                                        onError("Не удалось захватить изображение")
                                    }
                                } finally {
                                    isCapturing = false
                                }
                            } else if (cameraState.isFrozen) {
                                // Возвращаемся к live preview
                                viewModel.resumeCamera() // Перезапускаем камеру
                                delay(150) // Даем время на инициализацию
                                frozenBitmap = null
                                viewModel.setFrozen(false)
                            }
                        }
                    },
                    onFilterToggle = {
                        val nextFilter = when (cameraState.currentFilter) {
                            ru.wert.quickloupe.domain.models.FilterType.NORMAL ->
                                ru.wert.quickloupe.domain.models.FilterType.INVERTED
                            ru.wert.quickloupe.domain.models.FilterType.INVERTED ->
                                ru.wert.quickloupe.domain.models.FilterType.GRAYSCALE
                            ru.wert.quickloupe.domain.models.FilterType.GRAYSCALE ->
                                ru.wert.quickloupe.domain.models.FilterType.HIGH_CONTRAST
                            ru.wert.quickloupe.domain.models.FilterType.HIGH_CONTRAST ->
                                ru.wert.quickloupe.domain.models.FilterType.NORMAL
                        }
                        viewModel.setFilter(nextFilter)
                    },
                    onBackPressed = onBackPressed,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Индикатор загрузки при захвате кадра
        if (isCapturing) {
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
    previewView: android.view.View,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { _ -> previewView },
        modifier = modifier,
        update = { view ->
            // Force redraw
            view.invalidate()
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRequiredScreen(
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onBackPressed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Для работы приложения необходим доступ к камере",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text("Разрешить доступ к камере")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackPressed) {
            Text("Назад", color = Color.White)
        }

        // Если доступ отклонен, показываем дополнительное сообщение
        if (permissionState.status.shouldShowRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Доступ к камере необходим для использования лупы. Пожалуйста, разрешите доступ в настройках",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }

}