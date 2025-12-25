package ru.wert.quickloupe.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 12.dp), // Отступы вокруг слайдера
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Индикатор максимума (5x) - сверху
        Text(
            text = "5x",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Кастомный вертикальный слайдер
        CustomVerticalSlider(
            value = currentZoom,
            onValueChange = onZoomChanged,
            onValueChangeFinished = onZoomChangeFinished,
            valueRange = 1f..5f,
            modifier = Modifier
                .height(210.dp)
                .width(56.dp)
        )

        // Индикатор минимума (1x) - снизу
        Text(
            text = "1x",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
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
    trackHeight: Dp = 210.dp,
    thumbRadius: Dp = 14.dp,
    trackWidth: Dp = 6.dp,
    thumbWidth: Dp = 32.dp, // Ширина горизонтальной риски
    thumbHeight: Dp = 6.dp  // Высота горизонтальной риски
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

            // Рисуем фон трека (полупрозрачный)
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(centerX, 0f),
                end = Offset(centerX, height),
                strokeWidth = trackWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Рисуем активную часть трека (снизу вверх - от 1x до текущего значения)
            // Теперь активная часть - это пройденный путь от 1x до текущего значения
            val activeHeight = height * normalizedValue // Не инвертируем - активная часть снизу вверх
            val activeStartY = height - activeHeight // Начинаем снизу

            drawLine(
                color = colorScheme.primary.copy(alpha = 0.5f),
                start = Offset(centerX, activeStartY),
                end = Offset(centerX, height),
                strokeWidth = trackWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Рисуем горизонтальную риску вместо круга
            val thumbY = height - activeHeight // Позиция риски (центр)
            val thumbHalfWidth = thumbWidth.toPx() / 2
            val thumbHalfHeight = thumbHeight.toPx() / 2

            // Основная часть риски
            drawRect(
                color = colorScheme.primary,
                topLeft = Offset(centerX - thumbHalfWidth, thumbY - thumbHalfHeight),
                size = androidx.compose.ui.geometry.Size(thumbWidth.toPx(), thumbHeight.toPx())
            )

            // Дополнительная индикация при перетаскивании
            if (isDragging) {
                // Полупрозрачный фон под риской
                drawRect(
                    color = colorScheme.primary.copy(alpha = 0.3f),
                    topLeft = Offset(centerX - thumbHalfWidth * 1.5f, thumbY - thumbHalfHeight * 2f),
                    size = androidx.compose.ui.geometry.Size(
                        thumbWidth.toPx() * 1.5f,
                        thumbHeight.toPx() * 4f
                    ),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
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

    // Больше не инвертируем Y-координату
    // Теперь: 0 (верх) = 5x, totalHeight (низ) = 1x
    // Но мы хотим чтобы активная часть была снизу вверх
    val normalized = 1 - (yPosition / totalHeight).coerceIn(0f, 1f)

    // Плавное значение без округления до шагов
    return valueRange.start + normalized * (valueRange.endInclusive - valueRange.start)
}



