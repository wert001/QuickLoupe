package ru.wert.quickloupe.presentation.camera

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.wert.quickloupe.data.repository.CameraRepository
import ru.wert.quickloupe.di.CameraRepositoryFactory
import ru.wert.quickloupe.domain.models.CameraState
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repositoryFactory: CameraRepositoryFactory
) : ViewModel() {

    // Измените на nullable или используйте опциональное значение
    private var cameraRepository: CameraRepository? = null

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
        cameraRepository = repositoryFactory.create(lifecycleOwner)
        viewModelScope.launch {
            cameraRepository?.initializeCamera()
            // Обновляем состояние после инициализации
            cameraRepository?.getCameraState()?.collect { state ->
                _cameraState.value = state
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

    fun setFilter(filterType: ru.wert.quickloupe.domain.models.FilterType) {
        withRepository { it.setFilter(filterType) }
    }

    fun captureFrame() = cameraRepository?.captureFrame()

    fun setFrozen(frozen: Boolean) {
        cameraRepository?.setFrozen(frozen)
    }

    fun clearError() {
        cameraRepository?.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        cameraRepository?.release()
    }
}