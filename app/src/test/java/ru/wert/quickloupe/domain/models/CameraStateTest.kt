package ru.wert.quickloupe.domain.models

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CameraStateTest {

    @Test
    fun `camera state should have correct default values`() {
        val state = CameraState()

        assertEquals(1.0f, state.zoomLevel)
        assertEquals(FilterType.NORMAL, state.currentFilter)
        assertFalse(state.isFlashEnabled)
        assertFalse(state.isFrozen)
        assertTrue(state.isLoading)
        assertFalse(state.isInitialized)
        assertNull(state.error)
    }

    @Test
    fun `camera state copy should work correctly`() = runTest {
        val original = CameraState()

        val modified = original.copy(
            zoomLevel = 2.5f,
            currentFilter = FilterType.GRAYSCALE,
            isFlashEnabled = true,
            isFrozen = true,
            isLoading = false,
            isInitialized = true,
            error = "Test error"
        )

        assertEquals(2.5f, modified.zoomLevel)
        assertEquals(FilterType.GRAYSCALE, modified.currentFilter)
        assertTrue(modified.isFlashEnabled)
        assertTrue(modified.isFrozen)
        assertFalse(modified.isLoading)
        assertTrue(modified.isInitialized)
        assertEquals("Test error", modified.error)

        // Проверяем, что оригинал не изменился
        assertEquals(1.0f, original.zoomLevel)
        assertEquals(FilterType.NORMAL, original.currentFilter)
        assertFalse(original.isFlashEnabled)
        assertFalse(original.isFrozen)
        assertTrue(original.isLoading)
        assertFalse(original.isInitialized)
        assertNull(original.error)
    }

}