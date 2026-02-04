package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PauseCameraUseCase(
    private val cameraManager: CameraManagerImpl
) {
    suspend fun execute() {
        withContext(Dispatchers.Main) {
            try {
                cameraManager.unbindCamera()
            } catch (e: Exception) {
                cameraManager.getInternalState().update { it.copy(error = "Ошибка при паузе камеры") }
            }
        }
    }
}