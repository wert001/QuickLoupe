package ru.wert.quickloupe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Главный класс приложения.
 * Наследуется от Application и аннотируется @HiltAndroidApp для работы Hilt.
 * Этот класс должен быть указан в манифесте Android в теге <application>.
 *
 * Hilt использует этот класс для настройки внедрения зависимостей во всем приложении.
 */
@HiltAndroidApp // Аннотация Hilt для инициализации контейнера зависимостей приложения
class QuickLoupeApp : Application()