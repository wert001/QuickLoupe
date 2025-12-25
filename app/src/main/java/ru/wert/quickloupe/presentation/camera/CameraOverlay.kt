package ru.wert.quickloupe.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.wert.quickloupe.domain.models.CameraState
import ru.wert.quickloupe.presentation.common.components.ZoomSlider
import ru.wert.quickloupe.presentation.common.components.ControlButton

/**
 * Оверлей с элементами управления камерой
 * @param state текущее состояние камеры
 * @param onZoomChanged обработчик изменения уровня зума
 * @param onZoomChangeFinished обработчик завершения изменения зума
 * @param onFlashToggle обработчик переключения вспышки
 * @param onFreezeToggle обработчик заморозки/разморозки изображения
 * @param onFilterToggle обработчик переключения фильтров
 * @param onBackPressed обработчик нажатия кнопки "Назад"
 * @param onSwitchHandles обработчик переключения стороны управления
 * @param modifier модификатор компоновки
 */
@Composable
fun CameraOverlay(
    state: CameraState,
    onZoomChanged: (Float) -> Unit,
    onZoomChangeFinished: () -> Unit = {},
    onFlashToggle: () -> Unit,
    onFreezeToggle: () -> Unit,
    onFilterToggle: () -> Unit,
    onBackPressed: () -> Unit,
    onSwitchHandles: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Состояние для определения стороны управления (по умолчанию для правой руки)
    var isRightHandMode by remember { mutableStateOf(true) }

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

        // Нижняя панель
        BottomControls(
            state = state,
            onFreezeToggle = onFreezeToggle,
            onSwitchHandles = {
                isRightHandMode = !isRightHandMode
                onSwitchHandles()
            },
            isRightHandMode = isRightHandMode,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )

        // Вертикальный слайдер зума
        VerticalZoomSlider(
            currentZoom = state.zoomLevel,
            onZoomChanged = onZoomChanged,
            onZoomChangeFinished = onZoomChangeFinished,
            isRightSide = isRightHandMode,
            modifier = Modifier
                .align(if (isRightHandMode) {
                    Alignment.BottomEnd
                } else {
                    Alignment.BottomStart
                })
                .padding(
                    bottom = 160.dp, // Сдвиг вниз к нижней панели
                    end = if (isRightHandMode) 20.dp else 0.dp, // Отступ от правого края
                    start = if (!isRightHandMode) 20.dp else 0.dp // Отступ от левого края
                )
        )
    }
}

/**
 * Верхняя панель управления
 * @param state текущее состояние камеры
 * @param onFlashToggle обработчик переключения вспышки
 * @param onBackPressed обработчик нажатия кнопки "Назад"
 * @param modifier модификатор компоновки
 */
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

        // Индикатор зума с большей точностью
        Text(
            text = "${state.zoomLevel.format(2)}x",
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

/**
 * Нижняя панель управления
 * @param state текущее состояние камеры
 * @param onFreezeToggle обработчик заморозки/разморозки изображения
 * @param onSwitchHandles обработчик переключения стороны управления
 * @param isRightHandMode режим для правой руки (true) или левой (false)
 * @param modifier модификатор компоновки
 */
@Composable
private fun BottomControls(
    state: CameraState,
    onFreezeToggle: () -> Unit,
    onSwitchHandles: () -> Unit,
    isRightHandMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp) // Добавлены отступы как у верхней панели
            .background(Color.Black.copy(alpha = 0.3f))
            .clip(MaterialTheme.shapes.medium)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, // Равномерное распределение
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Левая часть: иконка переключения или пустое место
        if (isRightHandMode) {
            // Для правой руки: иконка слева
            ControlButton(
                icon = Icons.Default.ArrowForward,
                contentDescription = "Переключить управление на левую руку",
                onClick = onSwitchHandles
            )
        } else {
            // Для левой руки: пустое место слева для баланса
            Spacer(modifier = Modifier.size(56.dp)) // Размер как у ControlButton
        }

        // Центральная кнопка заморозки (всегда по центру)
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

        // Правая часть: иконка переключения или пустое место
        if (!isRightHandMode) {
            // Для левой руки: иконка справа
            ControlButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Переключить управление на правую руку",
                onClick = onSwitchHandles
            )
        } else {
            // Для правой руки: пустое место справа для баланса
            Spacer(modifier = Modifier.size(56.dp)) // Размер как у ControlButton
        }
    }
}

/**
 * Вертикальный слайдер зума
 * @param currentZoom текущий уровень зума
 * @param onZoomChanged обработчик изменения уровня зума
 * @param onZoomChangeFinished обработчик завершения изменения зума
 * @param isRightSide расположение слайдера (true - справа, false - слева)
 * @param modifier модификатор компоновки
 */
@Composable
private fun VerticalZoomSlider(
    currentZoom: Float,
    onZoomChanged: (Float) -> Unit,
    onZoomChangeFinished: () -> Unit = {},
    isRightSide: Boolean,
    modifier: Modifier = Modifier
) {
    ZoomSlider(
        currentZoom = currentZoom,
        onZoomChanged = onZoomChanged,
        onZoomChangeFinished = onZoomChangeFinished,
        isRightSide = isRightSide,
        modifier = modifier
    )
}

/**
 * Вспомогательная функция для форматирования чисел
 * @param digits количество знаков после запятой
 * @return отформатированная строка
 */
private fun Float.format(digits: Int = 1): String {
    return "%.${digits}f".format(this)
}

