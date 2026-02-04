package ru.wert.quickloupe.domain.usecases

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors

/**
 * Use case для инициализации камеры.
 * Получает провайдер камеры, настраивает executor и привязывает use cases.
 *
 * @param cameraManager менеджер камеры, который будет инициализирован
 */
class InitializeCameraUseCase(
    private val cameraManager: CameraManagerImpl
) {
    companion object {
        /** Минимальное значение зума */
        const val MIN_ZOOM = 1.0f

        /** Максимальное значение зума */
        const val MAX_ZOOM = 5.0f

        /** Значение зума по умолчанию */
        const val DEFAULT_ZOOM = 1.0f
    }

    /**
     * Инициализирует камеру.
     *
     * @param context контекст приложения
     * @param lifecycleOwner владелец жизненного цикла
     * @return true если инициализация прошла успешно, false в случае ошибки
     */
    suspend fun execute(context: Context, lifecycleOwner: LifecycleOwner): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Асинхронно получаем провайдер камеры
                val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(context)
                val cameraProvider = cameraProviderFuture.get()

                // Сохраняем провайдер в менеджере камеры
                cameraManager.setInternalCameraProvider(cameraProvider)

                // Создаем executor для обработки задач камеры
                val cameraExecutor = Executors.newSingleThreadExecutor()
                cameraManager.setInternalCameraExecutor(cameraExecutor)

                // Возвращаемся в главный поток для настройки UI
                withContext(Dispatchers.Main) {
                    cameraManager.bindCameraUseCases()
                }

                true
            } catch (e: Exception) {
                // В случае ошибки обновляем состояние камеры
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