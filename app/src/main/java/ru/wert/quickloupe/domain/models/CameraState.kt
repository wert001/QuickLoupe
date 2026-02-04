package ru.wert.quickloupe.domain.models

/**
 * Состояние камеры.
 * Модель данных, представляющая текущее состояние камеры и её настроек.
 * Используется для передачи состояния между слоями приложения.
 *
 * @property isLoading флаг загрузки (true - камера загружается)
 * @property isInitialized флаг инициализации (true - камера готова к использованию)
 * @property isFlashEnabled состояние вспышки (true - включена)
 * @property isFrozen состояние заморозки (true - изображение заморожено)
 * @property zoomLevel текущий уровень зума (от 1.0f до максимального значения)
 * @property error сообщение об ошибке (null если ошибок нет)
 */
data class CameraState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val isFrozen: Boolean = false,
    val zoomLevel: Float = 1.0f,
    val error: String? = null
)
