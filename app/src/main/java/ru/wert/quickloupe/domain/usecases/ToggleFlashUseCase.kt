package ru.wert.quickloupe.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Use case для управления вспышкой (фонариком) камеры.
 * Переключает состояние вспышки между включенным и выключенным.
 *
 * @param cameraManager менеджер камеры, предоставляющий доступ к внутренним компонентам
 */
class ToggleFlashUseCase(
    private val cameraManager: CameraManagerImpl
) {

    /**
     * Переключает состояние вспышки на противоположное.
     *
     * @return новое состояние вспышки (true - включена, false - выключена)
     *         или false, если операция не удалась
     */
    suspend fun execute(): Boolean {
        return try {
            // Получаем текущую камеру
            val camera = cameraManager.getInternalCamera() ?: return false

            // Определяем новое состояние вспышки (инвертируем текущее)
            val torchState = !cameraManager.getInternalState().value.isFlashEnabled

            // Включаем или выключаем вспышку в главном потоке
            withContext(Dispatchers.Main) {
                camera.cameraControl.enableTorch(torchState)
            }

            // Обновляем состояние в менеджере камеры
            cameraManager.getInternalState().update { it.copy(isFlashEnabled = torchState) }

            // Возвращаем новое состояние
            torchState
        } catch (e: Exception) {
            // В случае ошибки обновляем состояние с сообщением об ошибке
            cameraManager.getInternalState().update { it.copy(error = "Не удалось включить вспышку") }
            false
        }
    }
}