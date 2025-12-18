package ru.wert.quickloupe.presentation.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequiredScreen(
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Иконка камеры
        Icon(
            imageVector = Icons.Default.Face, //Здесь нужна иконка камеры
            contentDescription = "Камера",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Заголовок
        Text(
            text = "Для работы приложения нужна камера",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Описание
        Text(
            text = "Это приложение использует камеру вашего устройства " +
                    "для работы в качестве увеличительного стекла. " +
                    "Мы не сохраняем и не передаем изображения.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка разрешения
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Разрешить доступ к камере",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка назад
        TextButton(
            onClick = onBackPressed,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }

        // Если разрешение было отклонено
        if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Вы ранее отклонили разрешение. " +
                        "Чтобы использовать приложение, разрешите доступ в настройках.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}