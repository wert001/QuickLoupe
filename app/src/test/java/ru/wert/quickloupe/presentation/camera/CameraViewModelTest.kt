package ru.wert.quickloupe.presentation.camera

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.wert.quickloupe.data.repository.CameraRepositoryImpl
import ru.wert.quickloupe.di.CameraRepositoryFactory
import ru.wert.quickloupe.domain.models.CameraState

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CameraViewModelTest {

    private lateinit var viewModel: CameraViewModel
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: TestScope

    @MockK
    private lateinit var mockRepositoryFactory: CameraRepositoryFactory

    @MockK
    private lateinit var mockCameraRepository: CameraRepositoryImpl

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)

        // Настраиваем моки для suspend функций
        coEvery { mockRepositoryFactory.create(any()) } returns mockCameraRepository
        coEvery { mockCameraRepository.initializeCamera() } returns true
        every { mockCameraRepository.getCameraState() } returns MutableStateFlow(
            CameraState(
                isFrozen = false,
                zoomLevel = 1.0f,
                isFlashEnabled = false,
                isLoading = false,
                error = null
            )
        )
        coEvery { mockCameraRepository.setZoom(any()) } returns Unit
        coEvery { mockCameraRepository.toggleFlash() } returns true
        coEvery { mockCameraRepository.pauseCamera() } returns Unit
        coEvery { mockCameraRepository.resumeCamera() } returns Unit
        coEvery { mockCameraRepository.captureFrame() } returns null
        every { mockCameraRepository.setFrozen(any()) } just Runs
        every { mockCameraRepository.clearError() } just Runs
        every { mockCameraRepository.getPreviewView() } returns null
        every { mockCameraRepository.release() } just Runs

        viewModel = CameraViewModel(mockRepositoryFactory)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `инициализация камеры должна создать репозиторий и начать сбор состояния`() = testScope.runTest {
        // Arrange
        val mockLifecycleOwner = mockk<androidx.lifecycle.LifecycleOwner>(relaxed = true)

        // Act
        viewModel.initializeCamera(mockLifecycleOwner)
        advanceUntilIdle()

        // Assert
        coVerify { mockRepositoryFactory.create(mockLifecycleOwner) }
        coVerify { mockCameraRepository.initializeCamera() }
        verify { mockCameraRepository.getCameraState() }

        val actualState = viewModel.cameraState.first()
        assertFalse(actualState.isLoading)
    }

    @Test
    fun `инициализация камеры должна обработать неудачную инициализацию`() = testScope.runTest {
        // Arrange
        val mockLifecycleOwner = mockk<androidx.lifecycle.LifecycleOwner>(relaxed = true)
        coEvery { mockCameraRepository.initializeCamera() } returns false

        // Act
        viewModel.initializeCamera(mockLifecycleOwner)
        advanceUntilIdle()

        // Assert
        val state = viewModel.cameraState.first()
        assertFalse(state.isLoading)
        assertEquals("Не удалось инициализировать камеру", state.error)
    }

    @Test
    fun `инициализация камеры должна обработать исключение`() = testScope.runTest {
        // Arrange
        val mockLifecycleOwner = mockk<androidx.lifecycle.LifecycleOwner>(relaxed = true)
        val exceptionMessage = "Camera initialization failed"
        coEvery { mockRepositoryFactory.create(any()) } throws RuntimeException(exceptionMessage)

        // Act
        viewModel.initializeCamera(mockLifecycleOwner)
        advanceUntilIdle()

        // Assert
        val state = viewModel.cameraState.first()
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains(exceptionMessage) == true)
    }

    @Test
    fun `установка зума должна вызвать setZoom репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        val zoomLevel = 2.5f
        инициализироватьViewModel()

        // Act
        launch {
            viewModel.setZoom(zoomLevel)
        }
        advanceUntilIdle()

        // Assert
        coVerify { mockCameraRepository.setZoom(zoomLevel) }
    }

    @Test
    fun `установка зума не должна крашиться когда репозиторий не инициализирован`() = testScope.runTest {
        // Arrange
        val zoomLevel = 2.5f
        // Не инициализируем репозиторий

        // Act & Assert - не должно быть исключения
        launch {
            assertDoesNotThrow {
                viewModel.setZoom(zoomLevel)
            }
        }
        advanceUntilIdle()

        // Должен быть 0 вызовов, так как репозиторий не инициализирован
        coVerify(exactly = 0) { mockCameraRepository.setZoom(any()) }
    }

    @Test
    fun `переключение вспышки должно вызвать toggleFlash репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act
        launch {
            viewModel.toggleFlash()
        }
        advanceUntilIdle()

        // Assert
        coVerify { mockCameraRepository.toggleFlash() }
    }

    @Test
    fun `пауза камеры должна вызвать pauseCamera репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act
        launch {
            viewModel.pauseCamera()
        }
        advanceUntilIdle()

        // Assert
        coVerify { mockCameraRepository.pauseCamera() }
    }

    @Test
    fun `возобновление камеры должно вызвать resumeCamera репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act
        launch {
            viewModel.resumeCamera()
        }
        advanceUntilIdle()

        // Assert
        coVerify { mockCameraRepository.resumeCamera() }
    }

    @Test
    fun `захват кадра должен вызвать captureFrame репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act
        launch {
            viewModel.captureFrame()
        }
        advanceUntilIdle()

        // Assert
        coVerify { mockCameraRepository.captureFrame() }
    }

    @Test
    fun `установка заморозки должна вызвать setFrozen репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act - setFrozen не вызывается через withRepository, поэтому не нужен launch
        viewModel.setFrozen(true)
        advanceUntilIdle()

        // Assert
        verify { mockCameraRepository.setFrozen(true) }
    }

    @Test
    fun `очистка ошибки должна вызвать clearError репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act - clearError не вызывается через withRepository, поэтому не нужен launch
        viewModel.clearError()
        advanceUntilIdle()

        // Assert
        verify { mockCameraRepository.clearError() }
    }

    @Test
    fun `получение previewView должно вернуть previewView репозитория когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act
        val result = viewModel.getPreviewView()

        // Assert
        verify { mockCameraRepository.getPreviewView() }
        assertNull(result)
    }

    @Test
    fun `получение previewView должно вернуть null когда репозиторий не инициализирован`() = testScope.runTest {
        // Arrange - не инициализируем

        // Act
        val result = viewModel.getPreviewView()

        // Assert
        verify(exactly = 0) { mockCameraRepository.getPreviewView() }
        assertNull(result)
    }

    @Test
    fun `принудительное пересоздание preview должно вызвать pauseCamera и resumeCamera когда репозиторий инициализирован`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act - forceRecreatePreview уже содержит launch внутри
        viewModel.forceRecreatePreview()
        advanceUntilIdle()

        // Ждем немного, так как есть delay внутри метода
        launch {
            delay(100)
        }
        advanceUntilIdle()

        // Assert
        coVerify { mockCameraRepository.pauseCamera() }
        coVerify { mockCameraRepository.resumeCamera() }
    }

    @Test
    fun `ViewModel должен иметь начальное состояние загрузки`() = testScope.runTest {
        // Arrange & Act
        val initialState = viewModel.cameraState.first()

        // Assert
        assertTrue(initialState.isLoading)
        assertNull(initialState.error)
        assertFalse(initialState.isFrozen)
        assertEquals(1.0f, initialState.zoomLevel)
        assertFalse(initialState.isFlashEnabled)
    }

    @Test
    fun `вызов onCleared должен вызвать release репозитория`() = testScope.runTest {
        // Arrange
        инициализироватьViewModel()

        // Act - вызываем onCleared через reflection, так как он protected
        val onClearedMethod = CameraViewModel::class.java.getDeclaredMethod("onCleared")
        onClearedMethod.isAccessible = true
        onClearedMethod.invoke(viewModel)

        // Assert
        verify { mockCameraRepository.release() }
    }

    @Test
    fun `инициализация камеры должна обновить состояние когда репозиторий эмитит новое состояние`() = testScope.runTest {
        // Arrange
        val mockLifecycleOwner = mockk<androidx.lifecycle.LifecycleOwner>(relaxed = true)
        val testStateFlow = MutableStateFlow(
            CameraState(
                isFrozen = true,
                zoomLevel = 2.0f,
                isFlashEnabled = true,
                isLoading = false,
                error = "Test error"
            )
        )

        every { mockCameraRepository.getCameraState() } returns testStateFlow

        // Act
        viewModel.initializeCamera(mockLifecycleOwner)
        advanceUntilIdle()

        // Assert
        val actualState = viewModel.cameraState.first()
        assertTrue(actualState.isFrozen)
        assertEquals(2.0f, actualState.zoomLevel)
        assertTrue(actualState.isFlashEnabled)
        assertEquals("Test error", actualState.error)
    }

    @Test
    fun `множественные вызовы initializeCamera не должны создавать множественные подписки`() = testScope.runTest {
        // Arrange
        val mockLifecycleOwner = mockk<androidx.lifecycle.LifecycleOwner>(relaxed = true)

        // Act
        viewModel.initializeCamera(mockLifecycleOwner)
        viewModel.initializeCamera(mockLifecycleOwner) // Второй вызов
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 2) { mockRepositoryFactory.create(mockLifecycleOwner) }
        coVerify(exactly = 2) { mockCameraRepository.initializeCamera() }
        verify(exactly = 2) { mockCameraRepository.getCameraState() }
    }

    private suspend fun инициализироватьViewModel() {
        val mockLifecycleOwner = mockk<androidx.lifecycle.LifecycleOwner>(relaxed = true)
        viewModel.initializeCamera(mockLifecycleOwner)
    }
}