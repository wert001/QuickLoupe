package ru.wert.quickloupe.domain.usecases

import android.content.Context
import android.view.ViewGroup
import androidx.camera.view.PreviewView

/**
 * Use case для создания PreviewView.
 * Настраивает PreviewView для отображения потока с камеры.
 *
 * @param cameraManager менеджер камеры, в котором будет храниться PreviewView
 */
class CreatePreviewViewUseCase(
    private val cameraManager: CameraManagerImpl
) {

    /**
     * Создает и настраивает PreviewView.
     *
     * @param context контекст приложения
     * @return настроенный PreviewView
     */
    fun execute(context: Context): PreviewView {
        return PreviewView(context).apply {
            // Устанавливаем размеры на весь доступный экран
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Масштабирование изображения для заполнения всего view
            scaleType = PreviewView.ScaleType.FILL_CENTER

            // Режим совместимости для поддержки старых устройств
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
}