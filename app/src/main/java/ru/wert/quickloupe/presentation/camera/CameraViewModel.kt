package ru.wert.quickloupe.presentation.camera

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.launch
import ru.wert.quickloupe.data.repository.CameraRepository
import ru.wert.quickloupe.di.CameraRepositoryFactory
import ru.wert.quickloupe.domain.models.CameraState
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repositoryFactory: CameraRepositoryFactory
) : ViewModel() {

    private lateinit var cameraRepository: CameraRepository
    val cameraState: StateFlow<CameraState>
        get() = cameraRepository.getCameraState() as StateFlow<CameraState>

    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        cameraRepository = repositoryFactory.create(lifecycleOwner)
        viewModelScope.launch {
            cameraRepository.initializeCamera()
        }
    }

    fun setZoom(zoomLevel: Float) {
        viewModelScope.launch {
            cameraRepository.setZoom(zoomLevel)
        }
    }

    fun toggleFlash() {
        viewModelScope.launch {
            cameraRepository.toggleFlash()
        }
    }

    fun pauseCamera() {
        viewModelScope.launch {
            cameraRepository.pauseCamera()
        }
    }

    fun resumeCamera() {
        viewModelScope.launch {
            cameraRepository.resumeCamera()
        }
    }

    fun setFilter(filterType: ru.wert.quickloupe.domain.models.FilterType) {
        viewModelScope.launch {
            cameraRepository.setFilter(filterType)
        }
    }

    fun captureFrame() = cameraRepository.captureFrame()

    fun setFrozen(frozen: Boolean) {
        cameraRepository.setFrozen(frozen)
    }

    fun clearError() {
        cameraRepository.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        cameraRepository.release()
    }
}