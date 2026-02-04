package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Use case для приостановки работы камеры.
 * Освобождает ресурсы камеры при переходе приложения в фон.
 *
 * @param cameraManager менеджер камеры, который будет приостановлен
 */
class PauseCameraUseCase(
    private val cameraManager: CameraManagerImpl
) {

    /**
     * Приостанавливает работу камеры.
     * Выполняется в главном потоке для безопасного управления UI.
     */
    suspend fun execute() {
        withContext(Dispatchers.Main) {
            try {
                // Отвязываем все use cases от камеры
                cameraManager.unbindCamera()
            } catch (e: Exception) {
                cameraManager.getInternalState().update {
                    it.copy(error = "Ошибка при паузе камеры")
                }
            }
        }
    }
}