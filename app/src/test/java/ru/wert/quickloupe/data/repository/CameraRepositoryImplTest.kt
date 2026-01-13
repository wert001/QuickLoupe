package ru.wert.quickloupe.data.repository

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Тесты для класса CameraRepositoryImpl.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(io.mockk.junit5.MockKExtension::class)
class CameraRepositoryImplTest {

    private lateinit var mockContext: Context
    private lateinit var mockLifecycleOwner: LifecycleOwner
    private lateinit var repository: CameraRepositoryImpl

    @BeforeEach
    fun подготовить() {
        // Создаем полный мок Context с минимальными необходимыми методами
        mockContext = mockk<Context>(relaxed = true).apply {
            every { applicationContext } returns this
            every { packageName } returns "test.package"
            every { getSystemService(any()) } returns null
        }

        // Создаем мок LifecycleOwner
        mockLifecycleOwner = mockk<LifecycleOwner>(relaxed = true)

        // Создаем репозиторий
        repository = CameraRepositoryImpl(mockContext, mockLifecycleOwner)

        // Настраиваем Dispatchers для тестов
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun очистить() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `начальное состояние должно содержать значения по умолчанию`() {
        // Given
        // Новый репозиторий создан в @BeforeEach

        // When
        val initialState = repository.getCameraState().value

        // Then
        assertTrue(initialState.isLoading, "Должно быть в состоянии загрузки")
        assertNull(initialState.error, "Не должно быть ошибок")
        assertEquals(1.0f, initialState.zoomLevel, "Уровень зума по умолчанию должен быть 1.0")
        assertFalse(initialState.isFrozen, "Не должно быть заморожено")
        assertFalse(initialState.isFlashEnabled, "Вспышка должна быть выключена")
        assertFalse(initialState.isInitialized, "Камера не должна быть инициализирована")
    }
}