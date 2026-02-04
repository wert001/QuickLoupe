package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class SetZoomUseCase(
    private val cameraManager: CameraManagerImpl
) {
    suspend fun execute(zoomLevel: Float) {
        val clampedZoom = zoomLevel.coerceIn(InitializeCameraUseCase.MIN_ZOOM, InitializeCameraUseCase.MAX_ZOOM)

        try {
            withContext(Dispatchers.Main) {
                cameraManager.getInternalCamera()?.cameraControl?.setZoomRatio(clampedZoom)
                cameraManager.getInternalState().update { it.copy(zoomLevel = clampedZoom) }
            }
        } catch (e: Exception) {
            cameraManager.getInternalState().update { it.copy(error = "Ошибка изменения зума") }
        }
    }
}