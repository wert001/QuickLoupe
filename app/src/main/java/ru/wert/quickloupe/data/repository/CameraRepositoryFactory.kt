package ru.wert.quickloupe.data.repository

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject

interface CameraRepositoryFactory {
    fun create(lifecycleOwner: LifecycleOwner): CameraRepository
}

class CameraRepositoryFactoryImpl @Inject constructor(
    private val context: Context
) : CameraRepositoryFactory {
    override fun create(lifecycleOwner: LifecycleOwner): CameraRepository {
        return CameraRepositoryImpl(context, lifecycleOwner)
    }
}