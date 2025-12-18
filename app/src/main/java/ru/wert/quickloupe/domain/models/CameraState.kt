package ru.wert.quickloupe.domain.models

data class CameraState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val isFrozen: Boolean = false,
    val zoomLevel: Float = 1.0f,
    val currentFilter: FilterType = FilterType.NORMAL,
    val error: String? = null
)
