package ru.wert.quickloupe.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Вертикальный слайдер для управления зумом камеры
 * @param currentZoom текущий уровень зума (от 1.0f до 5.0f)
 * @param onZoomChanged обработчик изменения уровня зума
 * @param onZoomChangeFinished обработчик завершения изменения зума
 * @param isRightSide расположение слайдера: true - справа, false - слева
 * @param modifier модификатор компоновки
 */
@Composable
fun ZoomSlider(
    currentZoom: Float,
    onZoomChanged: (Float) -> Unit,
    onZoomChangeFinished: () -> Unit = {},
    isRightSide: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Индикатор максимума (5x) - сверху
        Text(
            text = "5x",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )

        // Кастомный вертикальный слайдер
        CustomVerticalSlider(
            value = currentZoom,
            onValueChange = onZoomChanged,
            onValueChangeFinished = onZoomChangeFinished,
            valueRange = 1f..5f,
            modifier = Modifier
                .height(280.dp) // Длина слайдера
                .width(60.dp)   // Ширина для удобного касания
        )

        // Индикатор минимума (1x) - снизу
        Text(
            text = "1x",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

/**
 * Кастомная реализация вертикального слайдера
 */
@Composable
private fun CustomVerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float> = 1f..5f,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 280.dp,
    thumbRadius: Dp = 16.dp,
    trackWidth: Dp = 6.dp
) {
    // Состояние для хранения размера компонента
    var componentSize by remember { mutableStateOf(Size.Zero) }

    // Локальное значение для плавного перетаскивания
    var currentValue by remember { mutableStateOf(value) }

    // Обновляем локальное значение при изменении внешнего значения
    LaunchedEffect(value) {
        currentValue = value
    }

    // Вычисляем позицию ползунка
    val normalizedValue = (currentValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)

    // Состояние для перетаскивания
    var isDragging by remember { mutableStateOf(false) }

    // Получаем цветовую схему
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                componentSize = Size(size.width.toFloat(), size.height.toFloat())
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newValue = calculateValueFromPosition(
                            offset.y,
                            componentSize.height,
                            valueRange
                        )
                        currentValue = newValue
                        onValueChange(newValue)
                    },
                    onDrag = { change, _ ->
                        val newValue = calculateValueFromPosition(
                            change.position.y,
                            componentSize.height,
                            valueRange
                        )
                        currentValue = newValue
                        onValueChange(newValue)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished()
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newValue = calculateValueFromPosition(
                        offset.y,
                        componentSize.height,
                        valueRange
                    )
                    currentValue = newValue
                    onValueChange(newValue)
                    onValueChangeFinished()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2

            // Рисуем фон трека
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(centerX, 0f),
                end = Offset(centerX, height),
                strokeWidth = trackWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Рисуем активную часть трека (сверху вниз)
            val activeHeight = height * (1 - normalizedValue) // Инвертируем для вертикального направления
            drawLine(
                color = colorScheme.primary,
                start = Offset(centerX, 0f),
                end = Offset(centerX, activeHeight),
                strokeWidth = trackWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Рисуем ползунок
            val thumbY = activeHeight
            drawCircle(
                color = if (isDragging) colorScheme.primary.copy(alpha = 0.8f)
                else colorScheme.primary,
                radius = thumbRadius.toPx(),
                center = Offset(centerX, thumbY)
            )

            // Внешний контур ползунка
            drawCircle(
                color = Color.White,
                radius = thumbRadius.toPx() + 1.dp.toPx(),
                center = Offset(centerX, thumbY),
                style = Stroke(width = 2.dp.toPx())
            )

            // Внутренний белый круг для лучшей видимости
            drawCircle(
                color = Color.White,
                radius = thumbRadius.toPx() * 0.4f,
                center = Offset(centerX, thumbY)
            )
        }
    }
}

/**
 * Вычисление значения на основе позиции касания
 */
private fun calculateValueFromPosition(
    yPosition: Float,
    totalHeight: Float,
    valueRange: ClosedFloatingPointRange<Float>
): Float {
    if (totalHeight <= 0) return valueRange.start

    // Инвертируем Y-координату (0 = максимум, totalHeight = минимум)
    val normalized = 1 - (yPosition / totalHeight).coerceIn(0f, 1f)

    // Плавное значение без округления до шагов
    return valueRange.start + normalized * (valueRange.endInclusive - valueRange.start)
}


