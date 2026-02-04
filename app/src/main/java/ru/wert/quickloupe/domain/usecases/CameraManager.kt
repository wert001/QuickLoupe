package ru.wert.quickloupe.domain.usecases

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import ru.wert.quickloupe.domain.models.CameraState

/**
 * Интерфейс менеджера камеры.
 * Определяет основные операции для работы с камерой устройства.
 */
interface CameraManager {

    /**
     * Инициализирует камеру устройства.
     *
     * @return true если инициализация прошла успешно, false в случае ошибки
     */
    suspend fun initializeCamera(): Boolean

    /**
     * Устанавливает уровень зума камеры.
     *
     * @param zoomLevel уровень зума (обычно в диапазоне от 1.0f до максимального значения)
     */
    suspend fun setZoom(zoomLevel: Float)

    /**
     * Переключает состояние вспышки (фонарика).
     *
     * @return новое состояние вспышки (true - включена, false - выключена)
     */
    suspend fun toggleFlash(): Boolean

    /**
     * Приостанавливает работу камеры.
     * Освобождает ресурсы камеры для экономии энергии.
     */
    suspend fun pauseCamera()

    /**
     * Возобновляет работу камеры после паузы.
     */
    suspend fun resumeCamera()

    /**
     * Захватывает текущий кадр с камеры.
     *
     * @return Bitmap захваченного изображения или null в случае ошибки
     */
    fun captureFrame(): Bitmap?

    /**
     * Получает состояние камеры как поток данных.
     *
     * @return Flow с состоянием камеры
     */
    fun getCameraState(): Flow<CameraState>

    /**
     * Замораживает или размораживает изображение с камеры.
     *
     * @param frozen true - заморозить изображение, false - разморозить
     */
    fun setFrozen(frozen: Boolean)

    /**
     * Очищает сообщение об ошибке в состоянии камеры.
     */
    fun clearError()

    /**
     * Освобождает все ресурсы камеры.
     * Должен вызываться при уничтожении компонента, использующего камеру.
     */
    fun release()
}