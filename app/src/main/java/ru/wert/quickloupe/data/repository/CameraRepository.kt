package ru.wert.quickloupe.data.repository

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface CameraRepository {
    suspend fun initializeCamera(): Boolean
    suspend fun setZoom(zoomLevel: Float)
    suspend fun toggleFlash(): Boolean
    suspend fun pauseCamera()
    suspend fun resumeCamera()
    suspend fun setFilter(filterType: ru.wert.quickloupe.domain.models.FilterType)
    fun captureFrame(): Bitmap?
    fun getCameraState(): Flow<ru.wert.quickloupe.domain.models.CameraState>
    fun setFrozen(frozen: Boolean)
    fun clearError()
    fun release()
}