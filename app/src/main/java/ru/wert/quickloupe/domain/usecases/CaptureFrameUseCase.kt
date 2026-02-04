package ru.wert.quickloupe.domain.usecases

import android.graphics.Bitmap

/**
 * Use case для захвата текущего кадра с камеры.
 * Получает изображение из PreviewView и создает копию Bitmap.
 *
 * @param cameraManager менеджер камеры, предоставляющий доступ к PreviewView
 */
class CaptureFrameUseCase(
    private val cameraManager: CameraManagerImpl
) {

    /**
     * Захватывает текущий кадр с PreviewView.
     *
     * @return Bitmap захваченного изображения или null в случае ошибки
     */
    fun execute(): Bitmap? {
        return try {
            // Получаем Bitmap из PreviewView и создаем копию
            // Копия необходима для безопасного использования вне UI потока
            cameraManager.getInternalPreviewView()?.bitmap?.copy(Bitmap.Config.ARGB_8888, false)
        } catch (e: Exception) {
            null
        }
    }
}