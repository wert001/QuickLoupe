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
import ru.wert.quickloupe.data.repository.CameraRepository
import ru.wert.quickloupe.data.repository.CameraRepositoryImpl
import ru.wert.quickloupe.di.CameraRepositoryFactory
import ru.wert.quickloupe.domain.models.CameraState
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repositoryFactory: CameraRepositoryFactory
) : ViewModel() {

    // Измените на nullable или используйте опциональное значение
    private var cameraRepository: CameraRepositoryImpl? = null

    // Создайте fallback состояние, пока камера не инициализирована
    private val _cameraState = MutableStateFlow(
        CameraState(
            isFrozen = false,
            currentFilter = ru.wert.quickloupe.domain.models.FilterType.NORMAL,
            zoomLevel = 1.0f,
            isFlashEnabled = false,
            isLoading = true, // Показываем загрузку до инициализации
            error = null
        )
    )
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            try {
                _cameraState.value = _cameraState.value.copy(isLoading = true)
                cameraRepository = repositoryFactory.create(lifecycleOwner) as? CameraRepositoryImpl

                val initialized = cameraRepository?.initializeCamera() ?: false

                if (initialized) {
                    // Подписываемся на обновления состояния
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

    private fun withRepository(action: suspend (CameraRepository) -> Unit) {
        viewModelScope.launch {
            cameraRepository?.let { action(it) }
        }
    }

    fun setZoom(zoomLevel: Float) {
        withRepository { it.setZoom(zoomLevel) }
    }

    fun toggleFlash() {
        withRepository { it.toggleFlash() }
    }

    fun pauseCamera() {
        withRepository { it.pauseCamera() }
    }

    fun resumeCamera() {
        withRepository { it.resumeCamera() }
    }

    fun captureFrame() = cameraRepository?.captureFrame()

    fun setFrozen(frozen: Boolean) {
        cameraRepository?.setFrozen(frozen)
    }

    fun clearError() {
        cameraRepository?.clearError()
    }

    fun getPreviewView(): View? {
        return cameraRepository?.getPreviewView()
    }

    override fun onCleared() {
        super.onCleared()
        cameraRepository?.release()
    }

    fun forceRecreatePreview() {
        // Принудительно пересоздаем preview при необходимости
        viewModelScope.launch {
            cameraRepository?.pauseCamera()
            delay(50)
            cameraRepository?.resumeCamera()
        }
    }
}