package ru.wert.quickloupe.di

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import ru.wert.quickloupe.data.repository.CameraRepository
import ru.wert.quickloupe.data.repository.CameraRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    @ViewModelScoped
    fun provideCameraRepository(
        @ApplicationContext context: Context
    ): CameraRepositoryFactory {
        return CameraRepositoryFactory(context)
    }
}

class CameraRepositoryFactory(
    private val context: Context
) {
    fun create(lifecycleOwner: LifecycleOwner): CameraRepository {
        return CameraRepositoryImpl(context, lifecycleOwner)
    }
}