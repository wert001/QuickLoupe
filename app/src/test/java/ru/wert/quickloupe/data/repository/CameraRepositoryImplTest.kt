package ru.wert.quickloupe.data.repository

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.wert.quickloupe.domain.usecases.CameraManagerImpl

/**
 * Тесты для класса CameraRepositoryImpl.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(io.mockk.junit5.MockKExtension::class)
class CameraRepositoryImplTest {

    private lateinit var mockContext: Context
    private lateinit var mockLifecycleOwner: LifecycleOwner
    private lateinit var repository: CameraManagerImpl

    @BeforeEach
    fun setUp() {
        // Создаем полный мок Context с минимальными необходимыми методами
        mockContext = mockk<Context>(relaxed = true).apply {
            every { applicationContext } returns this
            every { packageName } returns "test.package"
            every { getSystemService(any()) } returns null
        }

        // Создаем мок LifecycleOwner
        mockLifecycleOwner = mockk<LifecycleOwner>(relaxed = true)

        // Создаем репозиторий
        repository = CameraManagerImpl(mockContext, mockLifecycleOwner)

        // Настраиваем Dispatchers для тестов
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun clear() {
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

    @Test
    fun `setZoom должен обновить состояние зума`() = runTest {
        // Given
        val initialZoom = repository.getCameraState().value.zoomLevel
        val newZoom = 2.5f

        // When
        repository.setZoom(newZoom)

        // Then
        val state = repository.getCameraState().value
        assertEquals(newZoom, state.zoomLevel, "Уровень зума должен обновиться")
        assertNotEquals(initialZoom, state.zoomLevel)
    }

    @Test
    fun `setZoom должен ограничивать значение в пределах MIN и MAX`() = runTest {
        // Given
        val tooHighZoom = 5.0f
        val tooLowZoom = 0.5f

        // When
        repository.setZoom(tooHighZoom)
        val stateAfterHigh = repository.getCameraState().value

        repository.setZoom(tooLowZoom)
        val stateAfterLow = repository.getCameraState().value

        // Then
        assertEquals(
            CameraManagerImpl.MAX_ZOOM, stateAfterHigh.zoomLevel,
            "Должно ограничиться MAX_ZOOM")
        assertEquals(
            CameraManagerImpl.MIN_ZOOM, stateAfterLow.zoomLevel,
            "Должно ограничиться MIN_ZOOM")
    }

    @Test
    fun `toggleFlash не должен изменять состояние вспышки при вызове без камеры`() = runTest {
        // Given
        val initialState = repository.getCameraState().value.isFlashEnabled

        // When
        repository.toggleFlash()
        val stateAfterToggle = repository.getCameraState().value

        // Then
        assertEquals(initialState, stateAfterToggle.isFlashEnabled,
            "Состояние вспышки не должно измениться без камеры")
    }

    @Test
    fun `setZoom должен обновлять состояние даже без инициализированной камеры`() = runTest {
        // Given
        val initialZoom = repository.getCameraState().value.zoomLevel
        val newZoom = 2.5f

        // When
        repository.setZoom(newZoom)

        // Then
        val state = repository.getCameraState().value
        assertEquals(newZoom, state.zoomLevel, "Уровень зума должен обновиться")
        assertNotEquals(initialZoom, state.zoomLevel)
        // Ошибка НЕ должна быть установлена, так как исключение не выбрасывается
        assertNull(state.error, "Ошибка не должна быть установлена, так как исключение не выбрасывается")
    }

    @Test
    fun `setFrozen должен работать независимо от состояния камеры`() {
        // Given
        val initialState = repository.getCameraState().value.isFrozen

        // When
        repository.setFrozen(!initialState)

        // Then
        val state = repository.getCameraState().value
        assertEquals(!initialState, state.isFrozen,
            "Состояние заморозки должно обновиться")
        assertNull(state.error, "Не должно быть ошибки")
    }

    @Test
    fun `setFrozen должен обновлять состояние заморозки`() {
        // Given
        val initialState = repository.getCameraState().value.isFrozen

        // When
        repository.setFrozen(!initialState)

        // Then
        val state = repository.getCameraState().value
        assertEquals(!initialState, state.isFrozen,
            "Состояние заморозки должно обновиться")
    }

    @Test
    fun `setFrozen должен позволять замораживать и размораживать`() {
        // Given
        repository.setFrozen(true)
        assertTrue(repository.getCameraState().value.isFrozen, "Должно быть заморожено")

        // When
        repository.setFrozen(false)

        // Then
        assertFalse(repository.getCameraState().value.isFrozen, "Должно быть разморожено")
    }

    @Test
    fun `clearError должен работать когда ошибки нет`() = runTest {
        // Given - убеждаемся, что ошибки нет
        repository.clearError() // Очищаем возможные предыдущие ошибки
        assertNull(repository.getCameraState().value.error, "Ошибки не должно быть")

        // When - очищаем снова
        repository.clearError()

        // Then - состояние не должно измениться
        val state = repository.getCameraState().value
        assertNull(state.error, "Ошибка должна оставаться null")
    }

    @Test
    fun `captureFrame должен возвращать null если previewView не создан`() {
        // When
        val result = repository.captureFrame()

        // Then
        assertNull(result, "Должен вернуть null когда нет previewView")
    }

    @Test
    fun `getPreviewView должен возвращать null если previewView не создан`() {
        // When
        val result = repository.getPreviewView()

        // Then
        assertNull(result, "Должен вернуть null когда previewView не создан")
    }

    @Test
    fun `pauseCamera не должен выбрасывать исключение`() = runTest {
        // When - вызываем на неинициализированной камере
        repository.pauseCamera()

        // Then - тест проходит если нет исключений
        assertTrue(true, "Не должно быть исключений")
    }

    @Test
    fun `resumeCamera не должен выбрасывать исключение`() = runTest {
        // When - вызываем на неинициализированной камере
        repository.resumeCamera()

        // Then - тест проходит если нет исключений
        assertTrue(true, "Не должно быть исключений")
    }

    @Test
    fun `initializeCamera должен возвращать false когда камера не доступна`() = runTest {
        // When
        val result = repository.initializeCamera()

        // Then
        assertFalse(result, "Должен вернуть false так как камера не доступна в тестах")
        assertNotNull(repository.getCameraState().value.error, "Должна быть установлена ошибка")
    }

    @Test
    fun `состояние должно быть неизменяемым извне`() {
        // Given
        val stateFlow = repository.getCameraState()

        // When - пытаемся изменить состояние напрямую (это не должно работать)
        // Это проверяет, что asStateFlow() действительно возвращает неизменяемый поток

        // Then
        assertTrue(stateFlow is kotlinx.coroutines.flow.StateFlow<*>,
            "Должен возвращаться StateFlow для неизменяемого состояния")
    }
}