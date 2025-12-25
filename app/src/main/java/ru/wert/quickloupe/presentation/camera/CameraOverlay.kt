package ru.wert.quickloupe.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.wert.quickloupe.domain.models.CameraState
import ru.wert.quickloupe.presentation.common.components.ZoomSlider
import ru.wert.quickloupe.presentation.common.components.ControlButton

@Composable
fun CameraOverlay(
    state: CameraState,
    onZoomChanged: (Float) -> Unit,
    onFlashToggle: () -> Unit,
    onFreezeToggle: () -> Unit,
    onFilterToggle: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Верхняя панель
        TopControls(
            state = state,
            onFlashToggle = onFlashToggle,
            onBackPressed = onBackPressed,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        // Нижняя панель - УВЕЛИЧЕН padding снизу
        BottomControls(
            state = state,
            onZoomChanged = onZoomChanged,
            onFreezeToggle = onFreezeToggle,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )

        // УБРАН индикатор зума в центре
    }
}

@Composable
private fun TopControls(
    state: CameraState,
    onFlashToggle: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
            .background(Color.Black.copy(alpha = 0.3f))
            .clip(MaterialTheme.shapes.medium)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка назад
        ControlButton(
            icon = Icons.Default.ArrowBack,
            contentDescription = "Назад",
            onClick = onBackPressed
        )

        // Индикатор зума
        Text(
            text = "${state.zoomLevel.format()}x",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        // Кнопка вспышки
        ControlButton(
            icon = if (state.isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
            contentDescription = "Вспышка",
            onClick = onFlashToggle,
            isActive = state.isFlashEnabled
        )
    }
}

@Composable
private fun BottomControls(
    state: CameraState,
    onZoomChanged: (Float) -> Unit,
    onFreezeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Слайдер зума
        ZoomSlider(
            currentZoom = state.zoomLevel,
            onZoomChanged = onZoomChanged,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .clip(MaterialTheme.shapes.medium)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center, // Изменено с SpaceEvenly на Center
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Центральная кнопка заморозки
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onFreezeToggle,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (state.isFrozen) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (state.isFrozen) "Продолжить" else "Заморозить",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

// Вспомогательная функция для форматирования чисел
private fun Float.format(digits: Int = 1): String {
    return "%.${digits}f".format(this)
}