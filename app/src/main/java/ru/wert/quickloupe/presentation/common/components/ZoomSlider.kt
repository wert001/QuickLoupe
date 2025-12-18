package ru.wert.quickloupe.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ZoomSlider(
    currentZoom: Float,
    onZoomChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Индикаторы минимума и максимума
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "1x",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            Text(
                text = "10x",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Слайдер
        Slider(
            value = currentZoom,
            onValueChange = onZoomChanged,
            valueRange = 1f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}