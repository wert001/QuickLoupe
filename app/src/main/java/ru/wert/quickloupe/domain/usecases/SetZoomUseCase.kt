package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Use case для управления зумом камеры.
 * Устанавливает уровень зума с учетом минимальных и максимальных значений.
 *
 * @param cameraManager менеджер камеры, предоставляющий доступ к контролу камеры
 */
class SetZoomUseCase(
    private val cameraManager: CameraManagerImpl
) {

    /**
     * Устанавливает уровень зума камеры.
     *
     * @param zoomLevel желаемый уровень зума
     */
    suspend fun execute(zoomLevel: Float) {
        // Ограничиваем значение зума допустимыми пределами
        val clampedZoom = zoomLevel.coerceIn(
            InitializeCameraUseCase.MIN_ZOOM,
            InitializeCameraUseCase.MAX_ZOOM
        )

        try {
            // Устанавливаем зум в главном потоке
            withContext(Dispatchers.Main) {
                cameraManager.getInternalCamera()?.cameraControl?.setZoomRatio(clampedZoom)

                // Обновляем состояние камеры с новым значением зума
                cameraManager.getInternalState().update { it.copy(zoomLevel = clampedZoom) }
            }
        } catch (e: Exception) {
            cameraManager.getInternalState().update {
                it.copy(error = "Ошибка изменения зума")
            }
        }
    }
}