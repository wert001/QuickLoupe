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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickLoupeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen(
                        onError = { errorMessage ->
                            // Здесь можно показать Toast или диалог
                            println("Camera error: $errorMessage")
                        },
                        onBackPressed = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}