package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ToggleFlashUseCase(
    private val cameraManager: CameraManagerImpl
) {
    suspend fun execute(): Boolean {
        return try {
            val camera = cameraManager.getInternalCamera() ?: return false
            val torchState = !cameraManager.getInternalState().value.isFlashEnabled

            withContext(Dispatchers.Main) {
                camera.cameraControl.enableTorch(torchState)
            }

            cameraManager.getInternalState().update { it.copy(isFlashEnabled = torchState) }
            torchState
        } catch (e: Exception) {
            cameraManager.getInternalState().update { it.copy(error = "Не удалось включить вспышку") }
            false
        }
    }
}