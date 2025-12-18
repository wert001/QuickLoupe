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

@Composable
fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Black.copy(alpha = 0.5f)
    }

    val iconColor = if (isActive) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.9f)
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}