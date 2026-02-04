package ru.wert.quickloupe.presentation.camera

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.wert.quickloupe.domain.usecases.CameraManager
import ru.wert.quickloupe.domain.usecases.CameraManagerImpl
import ru.wert.quickloupe.di.CameraRepositoryFactory
import ru.wert.quickloupe.domain.models.CameraState
import javax.inject.Inject

/**
 * ViewModel для управления камерой.
 * Координирует взаимодействие между UI и бизнес-логикой камеры.
 * Использует Hilt для внедрения зависимостей.
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repositoryFactory: CameraRepositoryFactory
) : ViewModel() {

    // Репозиторий камеры (nullable, так как создается с lifecycleOwner)
    private var cameraRepository: CameraManagerImpl? = null

    // Состояние камеры с начальными значениями
    private val _cameraState = MutableStateFlow(
        CameraState(
            isFrozen = false,
            zoomLevel = 1.0f,
            isFlashEnabled = false,
            isLoading = true, // Показываем загрузку до инициализации
            error = null
        )
    )

    /** Публичный доступ к состоянию камеры */
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    /**
     * Инициализирует камеру с указанным владельцем жизненного цикла.
     *
     * @param lifecycleOwner владелец жизненного цикла (Activity или Fragment)
     */
    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            try {
                _cameraState.value = _cameraState.value.copy(isLoading = true)

                // Создаем репозиторий через фабрику
                cameraRepository = repositoryFactory.create(lifecycleOwner) as? CameraManagerImpl

                val initialized = cameraRepository?.initializeCamera() ?: false

                if (initialized) {
                    // Подписываемся на обновления состояния из репозитория
                    viewModelScope.launch {
                        cameraRepository?.getCameraState()?.collect { state ->
                            _cameraState.value = state
                        }
                    }
                } else {
                    _cameraState.value = _cameraState.value.copy(
                        isLoading = false,
                        error = "Не удалось инициализировать камеру"
                    )
                }
            } catch (e: Exception) {
                _cameraState.value = _cameraState.value.copy(
                    isLoading = false,
                    error = "Ошибка инициализации: ${e.message}"
                )
            }
        }
    }

    /**
     * Вспомогательная функция для выполнения операций с репозиторием.
     * Проверяет наличие репозитория и выполняет действие в корутине.
     *
     * @param action действие для выполнения с репозиторием
     */
    private fun withRepository(action: suspend (CameraManager) -> Unit) {
        viewModelScope.launch {
            cameraRepository?.let { action(it) }
        }
    }

    /** Устанавливает уровень зума камеры */
    fun setZoom(zoomLevel: Float) {
        withRepository { it.setZoom(zoomLevel) }
    }

    /** Переключает состояние вспышки */
    fun toggleFlash() {
        withRepository { it.toggleFlash() }
    }

    /** Приостанавливает работу камеры */
    fun pauseCamera() {
        withRepository { it.pauseCamera() }
    }

    /** Возобновляет работу камеры после паузы */
    fun resumeCamera() {
        withRepository { it.resumeCamera() }
    }

    /** Захватывает текущий кадр с камеры */
    fun captureFrame() = cameraRepository?.captureFrame()

    /** Устанавливает состояние заморозки изображения */
    fun setFrozen(frozen: Boolean) {
        cameraRepository?.setFrozen(frozen)
    }

    /** Очищает сообщение об ошибке */
    fun clearError() {
        cameraRepository?.clearError()
    }

    /** Получает PreviewView для отображения в UI */
    fun getPreviewView(): View? {
        return cameraRepository?.getPreviewView()
    }

    /** Освобождает ресурсы при уничтожении ViewModel */
    override fun onCleared() {
        super.onCleared()
        cameraRepository?.release()
    }

    /** Принудительно пересоздает preview (используется для решения проблем с отображением) */
    fun forceRecreatePreview() {
        viewModelScope.launch {
            cameraRepository?.pauseCamera()
            delay(50)
            cameraRepository?.resumeCamera()
        }
    }
}