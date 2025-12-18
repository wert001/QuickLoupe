package ru.wert.quickloupe.presentation.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    companion object {
        private const val TAG = "CameraController"
        const val MIN_ZOOM = 1.0f
        const val MAX_ZOOM = 10.0f
        const val DEFAULT_ZOOM = 1.0f
    }

    // Состояние камеры
    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    // CameraX компоненты
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService

    // Preview view
    private var previewView: PreviewView? = null
    private var isPaused: Boolean = false

    suspend fun initialize(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting camera initialization...")

                // Получаем camera provider асинхронно
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProvider = cameraProviderFuture.get()

                cameraExecutor = Executors.newSingleThreadExecutor()

                // Возвращаемся в главный поток для настройки UI
                withContext(Dispatchers.Main) {
                    setupCamera()
                }

                Log.d(TAG, "Camera initialized successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                _state.update { it.copy(
                    error = "Не удалось инициализировать камеру: ${e.localizedMessage}",
                    isLoading = false
                ) }
                false
            }
        }
    }

    private fun setupCamera() {
        if (isPaused) {
            Log.d(TAG, "Camera is paused, skipping setup")
            return
        }

        val cameraProvider = cameraProvider ?: run {
            Log.e(TAG, "Camera provider is null")
            _state.update { it.copy(
                error = "Камера недоступна",
                isLoading = false
            ) }
            return
        }

        val previewView = previewView ?: run {
            Log.e(TAG, "PreviewView is null, creating new one")
            createPreviewView(context)
        }

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Получаем rotation из WindowManager
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val rotation = windowManager.defaultDisplay.rotation

            // Preview
            preview = Preview.Builder()
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Выбор камеры (задняя)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )

            // Настройка зума
            camera?.cameraControl?.setZoomRatio(DEFAULT_ZOOM)

            _state.update {
                it.copy(
                    isLoading = false,
                    isInitialized = true,
                    zoomLevel = DEFAULT_ZOOM,
                    error = null
                )
            }

            Log.d(TAG, "Camera setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
            _state.update { it.copy(
                error = "Не удалось подключить камеру: ${e.localizedMessage}",
                isLoading = false
            ) }
        }
    }

    suspend fun pauseCamera() {
        withContext(Dispatchers.Main) {
            try {
                isPaused = true
                cameraProvider?.unbindAll()
                camera = null
                preview = null
                Log.d(TAG, "Camera paused")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to pause camera", e)
            }
        }
    }

    suspend fun resumeCamera() {
        withContext(Dispatchers.Main) {
            try {
                isPaused = false
                setupCamera()
                Log.d(TAG, "Camera resumed")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resume camera", e)
            }
        }
    }

    suspend fun setZoom(zoomLevel: Float) {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM, MAX_ZOOM)

        try {
            withContext(Dispatchers.Main) {
                camera?.cameraControl?.setZoomRatio(clampedZoom)
                _state.update { it.copy(zoomLevel = clampedZoom) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Zoom failed", e)
            _state.update { it.copy(error = "Ошибка изменения зума") }
        }
    }

    suspend fun toggleFlash(): Boolean {
        return try {
            val camera = camera ?: return false
            val torchState = !_state.value.isFlashEnabled

            withContext(Dispatchers.Main) {
                camera.cameraControl.enableTorch(torchState)
            }

            _state.update { it.copy(isFlashEnabled = torchState) }
            torchState
        } catch (e: Exception) {
            Log.e(TAG, "Flash toggle failed", e)
            _state.update { it.copy(error = "Не удалось включить вспышку") }
            false
        }
    }

    fun setFrozen(frozen: Boolean) {
        _state.update { it.copy(isFrozen = frozen) }
    }

    suspend fun setFilter(filterType: FilterType) {
        // Здесь будет применение фильтров к изображению
        _state.update { it.copy(currentFilter = filterType) }
    }

    fun captureFrame(): Bitmap? {
        return try {
            // Даем время для стабилизации изображения
            Thread.sleep(100)
            previewView?.bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture frame", e)
            null
        }
    }

    fun createPreviewView(context: Context): PreviewView {
        previewView = PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
        return previewView!!
    }

    fun getPreviewView(): PreviewView? {
        return previewView
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun release() {
        try {
            cameraExecutor.shutdown()
            cameraProvider?.unbindAll()
            previewView = null
            _state.update { CameraState() }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing camera", e)
        }
    }
}

// Состояние камеры
data class CameraState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val isFrozen: Boolean = false,
    val zoomLevel: Float = CameraController.DEFAULT_ZOOM,
    val currentFilter: FilterType = FilterType.NORMAL,
    val error: String? = null
)

// Типы фильтров
enum class FilterType {
    NORMAL,
    INVERTED,
    GRAYSCALE,
    HIGH_CONTRAST
}