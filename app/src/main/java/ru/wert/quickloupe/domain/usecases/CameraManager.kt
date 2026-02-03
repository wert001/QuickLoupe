package ru.wert.quickloupe.domain.usecases

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import ru.wert.quickloupe.domain.models.CameraState

interface CameraManager {
    suspend fun initializeCamera(): Boolean
    suspend fun setZoom(zoomLevel: Float)
    suspend fun toggleFlash(): Boolean
    suspend fun pauseCamera()
    suspend fun resumeCamera()
    fun captureFrame(): Bitmap?
    fun getCameraState(): Flow<CameraState>
    fun setFrozen(frozen: Boolean)
    fun clearError()
    fun release()
}