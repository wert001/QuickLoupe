package ru.wert.quickloupe.di

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import ru.wert.quickloupe.domain.usecases.CameraManager
import ru.wert.quickloupe.domain.usecases.CameraManagerImpl

/**
 * Модуль зависимостей для приложения.
 * Используется для предоставления зависимостей через Dagger Hilt.
 */
@Module
@InstallIn(ViewModelComponent::class) // Устанавливаем модуль в компонент ViewModel
object AppModule {

    /**
     * Предоставляет фабрику для создания репозитория камеры.
     *
     * @param context контекст приложения
     * @return фабрика для создания CameraManager
     */
    @Provides
    @ViewModelScoped // Область видимости - одна ViewModel
    fun provideCameraRepository(
        @ApplicationContext context: Context
    ): CameraRepositoryFactory {
        return CameraRepositoryFactory(context)
    }
}

/**
 * Фабрика для создания менеджера камеры.
 * Позволяет создавать экземпляр CameraManager с конкретным lifecycleOwner.
 *
 * @property context контекст приложения
 */
class CameraRepositoryFactory(
    private val context: Context
) {

    /**
     * Создает экземпляр менеджера камеры.
     *
     * @param lifecycleOwner владелец жизненного цикла (Activity или Fragment)
     * @return реализация CameraManager
     */
    fun create(lifecycleOwner: LifecycleOwner): CameraManager {
        return CameraManagerImpl(context, lifecycleOwner)
    }
}