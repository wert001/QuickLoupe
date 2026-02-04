package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ResumeCameraUseCase(
    private val cameraManager: CameraManagerImpl
) {
    suspend fun execute() {
        withContext(Dispatchers.Main) {
            try {
                // Не используем Thread.sleep в корутинах
                delay(100)
                cameraManager.bindCameraUseCases()
            } catch (e: Exception) {
                cameraManager.getInternalState().update { it.copy(error = "Ошибка при возобновлении камеры") }
            }
        }
    }
}