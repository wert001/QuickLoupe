package ru.wert.quickloupe.domain.usecases

import android.graphics.Bitmap

class CaptureFrameUseCase(
    private val cameraManager: CameraManagerImpl
) {
    fun execute(): Bitmap? {
        return try {
            cameraManager.getInternalPreviewView()?.bitmap?.copy(Bitmap.Config.ARGB_8888, false)
        } catch (e: Exception) {
            null
        }
    }
}