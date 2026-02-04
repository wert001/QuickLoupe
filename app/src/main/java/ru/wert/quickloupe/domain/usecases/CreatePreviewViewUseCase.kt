package ru.wert.quickloupe.domain.usecases

import android.content.Context
import android.view.ViewGroup
import androidx.camera.view.PreviewView

class CreatePreviewViewUseCase(
    private val cameraManager: CameraManagerImpl
) {
    fun execute(context: Context): PreviewView {
        return PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
}