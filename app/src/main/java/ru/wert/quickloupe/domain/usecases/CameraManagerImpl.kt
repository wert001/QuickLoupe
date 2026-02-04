package ru.wert.quickloupe.domain.usecases

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Реализация репозитория для работы с камерой
 * Использует CameraX для управления камерой устройства
 */
class CameraManagerImpl(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : CameraManager {
    companion object {
        private const val TAG = "CameraRepository"
    }

    // Состояние камеры
    private val _state = MutableStateFlow(CameraState())
    override fun getCameraState(): StateFlow<CameraState> = _state.asStateFlow()

    // CameraX компоненты
    private var _cameraProvider: ProcessCameraProvider? = null
    private var _camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService

    // Use cases
    private val initializeCameraUseCase = InitializeCameraUseCase(this)
    private val setZoomUseCase = SetZoomUseCase(this)
    private val toggleFlashUseCase = ToggleFlashUseCase(this)
    private val pauseCameraUseCase = PauseCameraUseCase(this)
    private val resumeCameraUseCase = ResumeCameraUseCase(this)
    private val captureFrameUseCase = CaptureFrameUseCase(this)
    private val createPreviewViewUseCase = CreatePreviewViewUseCase(this)

    // Preview view
    private var previewView: PreviewView? = null
    private var isCameraBound: Boolean = false

    /**
     * Инициализация камеры
     * @return true если инициализация прошла успешно, false в случае ошибки
     */
    override suspend fun initializeCamera(): Boolean {
        return initializeCameraUseCase.execute(context, lifecycleOwner)
    }

    /**
     * Привязка use cases к камере
     */
    internal fun bindCameraUseCases() {
        val cameraProvider = _cameraProvider ?: run {
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
            _camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )

            // Настройка зума с учетом нового максимального значения
            _camera?.cameraControl?.setZoomRatio(_state.value.zoomLevel.coerceIn(InitializeCameraUseCase.MIN_ZOOM, InitializeCameraUseCase.MAX_ZOOM))

            // Восстанавливаем состояние вспышки
            if (_state.value.isFlashEnabled) {
                _camera?.cameraControl?.enableTorch(true)
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

    /**
     * Отключение всех use cases от камеры
     */
    internal fun unbindCamera() {
        try {
            _cameraProvider?.unbindAll()
            _camera = null
            preview = null
            isCameraBound = false
        } catch (e: Exception) {
            // Игнорируем ошибки при отключении
        }
    }

    /**
     * Приостановка работы камеры
     */
    override suspend fun pauseCamera() {
        pauseCameraUseCase.execute()
    }

    /**
     * Возобновление работы камеры
     */
    override suspend fun resumeCamera() {
        resumeCameraUseCase.execute()
    }

    /**
     * Установка уровня зума
     * @param zoomLevel уровень зума от 1.0f до 5.0f
     */
    override suspend fun setZoom(zoomLevel: Float) {
        setZoomUseCase.execute(zoomLevel)
    }

    /**
     * Переключение вспышки
     * @return новое состояние вспышки (true - включена, false - выключена)
     */
    override suspend fun toggleFlash(): Boolean {
        return toggleFlashUseCase.execute()
    }

    /**
     * Заморозка/разморозка изображения
     * @param frozen true - заморозить, false - разморозить
     */
    override fun setFrozen(frozen: Boolean) {
        _state.update { it.copy(isFrozen = frozen) }
    }

    /**
     * Захват текущего кадра
     * @return Bitmap захваченного изображения или null в случае ошибки
     */
    override fun captureFrame(): Bitmap? {
        return captureFrameUseCase.execute()
    }

    /**
     * Создание preview view для отображения потока с камеры
     */
    private fun createPreviewView() {
        previewView = createPreviewViewUseCase.execute(context)
    }

    /**
     * Получение preview view
     * @return PreviewView или null если не создан
     */
    fun getPreviewView(): PreviewView? = previewView

    /**
     * Очистка сообщения об ошибке
     */
    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Освобождение ресурсов камеры
     */
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

    // Internal getters for use cases
    internal fun getInternalState(): MutableStateFlow<CameraState> = _state
    internal fun getInternalCamera(): Camera? = _camera
    internal fun getInternalCameraProvider(): ProcessCameraProvider? = _cameraProvider
    internal fun getInternalCameraExecutor(): ExecutorService = cameraExecutor
    internal fun setInternalCameraProvider(provider: ProcessCameraProvider) {
        _cameraProvider = provider
    }
    internal fun setInternalCameraExecutor(executor: ExecutorService) {
        cameraExecutor = executor
    }
    internal fun isCameraBoundInternal(): Boolean = isCameraBound
    internal fun setCameraBoundInternal(bound: Boolean) {
        isCameraBound = bound
    }
    internal fun getInternalPreviewView(): PreviewView? = previewView
    internal fun setInternalPreviewView(view: PreviewView?) {
        previewView = view
    }
}