package ru.wert.quickloupe.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.wert.quickloupe.domain.models.CameraState
import ru.wert.quickloupe.domain.models.FilterType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraRepositoryImpl(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : CameraRepository {
    companion object {
        private const val TAG = "CameraRepository"
        const val MIN_ZOOM = 1.0f
        const val MAX_ZOOM = 10.0f
        const val DEFAULT_ZOOM = 1.0f
    }

    // Состояние камеры
    private val _state = MutableStateFlow(CameraState())
    override fun getCameraState(): StateFlow<CameraState> = _state.asStateFlow()

    // CameraX компоненты
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService

    // Preview view
    private var previewView: PreviewView? = null
    private var isCameraBound: Boolean = false

    override suspend fun initializeCamera(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Получаем camera provider асинхронно
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProvider = cameraProviderFuture.get()

                cameraExecutor = Executors.newSingleThreadExecutor()

                // Возвращаемся в главный поток для настройки UI
                withContext(Dispatchers.Main) {
                    bindCameraUseCases()
                }

                true
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Не удалось инициализировать камеру: ${e.localizedMessage}",
                    isLoading = false
                ) }
                false
            }
        }
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: run {
            _state.update { it.copy(
                error = "Камера недоступна",
                isLoading = false
            ) }
            return
        }

        try {
            // Сначала отключаем все use cases
            unbindCamera()

            // Создаем preview view если нужно
            if (previewView == null) {
                createPreviewView()
            }

            // Preview
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView!!.surfaceProvider)
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
            camera?.cameraControl?.setZoomRatio(_state.value.zoomLevel)

            // Восстанавливаем состояние вспышки
            if (_state.value.isFlashEnabled) {
                camera?.cameraControl?.enableTorch(true)
            }

            isCameraBound = true

            _state.update {
                it.copy(
                    isLoading = false,
                    isInitialized = true,
                    error = null
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(
                error = "Не удалось подключить камеру: ${e.localizedMessage}",
                isLoading = false
            ) }
        }
    }

    private fun unbindCamera() {
        try {
            cameraProvider?.unbindAll()
            camera = null
            preview = null
            isCameraBound = false
        } catch (e: Exception) {
            // Игнорируем ошибки при отключении
        }
    }

    override suspend fun pauseCamera() {
        withContext(Dispatchers.Main) {
            try {
                unbindCamera()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Ошибка при паузе камеры") }
            }
        }
    }

    override suspend fun resumeCamera() {
        withContext(Dispatchers.Main) {
            try {
                // Не используем Thread.sleep в корутинах
                delay(100)
                bindCameraUseCases()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Ошибка при возобновлении камеры") }
            }
        }
    }

    override suspend fun setZoom(zoomLevel: Float) {
        val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM, MAX_ZOOM)

        try {
            withContext(Dispatchers.Main) {
                camera?.cameraControl?.setZoomRatio(clampedZoom)
                _state.update { it.copy(zoomLevel = clampedZoom) }
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Ошибка изменения зума") }
        }
    }

    override suspend fun toggleFlash(): Boolean {
        return try {
            val camera = camera ?: return false
            val torchState = !_state.value.isFlashEnabled

            withContext(Dispatchers.Main) {
                camera.cameraControl.enableTorch(torchState)
            }

            _state.update { it.copy(isFlashEnabled = torchState) }
            torchState
        } catch (e: Exception) {
            _state.update { it.copy(error = "Не удалось включить вспышку") }
            false
        }
    }

    override fun setFrozen(frozen: Boolean) {
        _state.update { it.copy(isFrozen = frozen) }
    }

    override suspend fun setFilter(filterType: FilterType) {
        // Здесь будет применение фильтров к изображению
        _state.update { it.copy(currentFilter = filterType) }
    }

    override fun captureFrame(): Bitmap? {
        return try {
            // Вместо Thread.sleep используем небольшую паузу
            // или лучше - сделать это асинхронно
            previewView?.bitmap?.copy(Bitmap.Config.ARGB_8888, false)
        } catch (e: Exception) {
            null
        }
    }

    fun createPreviewView(): PreviewView {
        previewView = PreviewView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
        return previewView!!
    }

    fun getPreviewView(): PreviewView? = previewView

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun release() {
        try {
            unbindCamera()
            cameraExecutor.shutdown()
            previewView = null
            _state.update { CameraState() }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Ошибка освобождения ресурсов") }
        }
    }
}