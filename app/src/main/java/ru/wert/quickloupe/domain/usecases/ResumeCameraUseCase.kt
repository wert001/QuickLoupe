package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Use case для возобновления работы камеры после паузы.
 * Повторно привязывает use cases к камере.
 *
 * @param cameraManager менеджер камеры, который будет возобновлен
 */
class ResumeCameraUseCase(
    private val cameraManager: CameraManagerImpl
) {

    /**
     * Возобновляет работу камеры.
     * Выполняется в главном потоке для безопасного управления UI.
     */
    suspend fun execute() {
        withContext(Dispatchers.Main) {
            try {
                // Небольшая задержка для стабильности
                // Используем delay вместо Thread.sleep в корутинах
                delay(100)

                // Привязываем use cases к камере
                cameraManager.bindCameraUseCases()
            } catch (e: Exception) {
                cameraManager.getInternalState().update {
                    it.copy(error = "Ошибка при возобновлении камеры")
                }
            }
        }
    }
}