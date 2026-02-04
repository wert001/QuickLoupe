package ru.wert.quickloupe.domain.usecases

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors

class InitializeCameraUseCase(
    private val cameraManager: CameraManagerImpl
) {
    companion object {
        const val MIN_ZOOM = 1.0f
        const val MAX_ZOOM = 5.0f
        const val DEFAULT_ZOOM = 1.0f
    }

    suspend fun execute(context: Context, lifecycleOwner: LifecycleOwner): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Получаем camera provider асинхронно
                val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(context)
                val cameraProvider = cameraProviderFuture.get()
                cameraManager.setInternalCameraProvider(cameraProvider)

                val cameraExecutor = Executors.newSingleThreadExecutor()
                cameraManager.setInternalCameraExecutor(cameraExecutor)

                // Возвращаемся в главный поток для настройки UI
                withContext(Dispatchers.Main) {
                    cameraManager.bindCameraUseCases()
                }

                true
            } catch (e: Exception) {
                cameraManager.getInternalState().update {
                    it.copy(
                        error = "Не удалось инициализировать камеру: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
                false
            }
        }
    }
}