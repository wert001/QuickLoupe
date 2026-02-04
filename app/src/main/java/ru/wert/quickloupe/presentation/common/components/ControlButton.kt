package ru.wert.quickloupe.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Кастомная кнопка управления для элементов интерфейса камеры.
 * Имеет круглую форму и изменяет цвет в зависимости от состояния (активна/неактивна).
 *
 * @param icon иконка для отображения на кнопке
 * @param contentDescription описание для accessibility (озвучка для слабовидящих)
 * @param onClick обработчик нажатия на кнопку
 * @param isActive флаг активного состояния кнопки (меняет цвет)
 * @param modifier модификатор компоновки
 */
@Composable
fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Выбор цвета фона в зависимости от состояния
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primary // Основной цвет темы для активного состояния
    } else {
        Color.Black.copy(alpha = 0.5f) // Полупрозрачный черный для неактивного состояния
    }

    // Выбор цвета иконки в зависимости от состояния
    val iconColor = if (isActive) {
        Color.White // Белый для активного состояния
    } else {
        Color.White.copy(alpha = 0.9f) // Полупрозрачный белый для неактивного состояния
    }

    // Круглая кнопка с иконкой
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp) // Фиксированный размер
            .background(backgroundColor, CircleShape) // Круглый фон
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(28.dp) // Размер иконки
        )
    }
}