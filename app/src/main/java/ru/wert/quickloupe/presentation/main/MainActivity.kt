package ru.wert.quickloupe.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ru.wert.quickloupe.presentation.camera.CameraScreen
import ru.wert.quickloupe.presentation.theme.QuickLoupeTheme

/**
 * Главная Activity приложения.
 * Является точкой входа в приложение и отображает экран камеры.
 * Использует Hilt для внедрения зависимостей.
 */
@AndroidEntryPoint // Аннотация Hilt для активации внедрения зависимостей в Activity
class MainActivity : ComponentActivity() {

    /**
     * Вызывается при создании Activity.
     * Настраивает пользовательский интерфейс с помощью Jetpack Compose.
     *
     * @param savedInstanceState сохраненное состояние Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем Compose UI
        setContent {
            // Применяем пользовательскую тему приложения
            QuickLoupeTheme {
                // Основная поверхность, заполняющая весь экран
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Экран камеры - основной UI компонент приложения
                    CameraScreen(
                        onError = { errorMessage ->
                            // Обработчик ошибок камеры
                            // В реальном приложении здесь можно показать Toast или диалог
                            println("Camera error: $errorMessage")
                        },
                        onBackPressed = {
                            // Обработчик нажатия кнопки "Назад" - закрываем приложение
                            finish()
                        }
                    )
                }
            }
        }
    }
}