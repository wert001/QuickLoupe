package ru.wert.quickloupe.presentation.camera

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import ru.wert.quickloupe.data.repository.CameraRepositoryImpl
import ru.wert.quickloupe.di.CameraRepositoryFactory
import ru.wert.quickloupe.domain.models.CameraState
import java.lang.reflect.Method

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, MockitoExtension::class)
class CameraViewModelTest {

    @Mock
    private lateinit var mockRepositoryFactory: CameraRepositoryFactory

    @Mock
    private lateinit var mockCameraRepository: CameraRepositoryImpl

    @Mock
    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var viewModel: CameraViewModel
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScheduler: TestCoroutineScheduler

    @BeforeEach
    fun setUp() {
        // Настройка тестовых диспетчеров для корутин
        testScheduler = TestCoroutineScheduler()
        testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        // Настройка моков
        `when`(mockRepositoryFactory.create(mockLifecycleOwner)).thenReturn(mockCameraRepository)

        // Создаем ViewModel
        viewModel = CameraViewModel(mockRepositoryFactory)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initializeCamera should set loading state and then success state`() = runTest(testDispatcher) {
        // Given
        val expectedState = CameraState(
            isFrozen = false,
            zoomLevel = 1.0f,
            isFlashEnabled = false,
            isLoading = false,
            isInitialized = true,
            error = null
        )

        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(expectedState))

        // When
        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // Then
        val actualState = viewModel.cameraState.first()
        assertFalse(actualState.isLoading)
        assertTrue(actualState.isInitialized)
        assertNull(actualState.error)
        verify(mockRepositoryFactory).create(mockLifecycleOwner)
        verify(mockCameraRepository).initializeCamera()
    }

    @Test
    fun `initializeCamera should set error state when initialization fails`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(false)

        // When
        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // Then
        val actualState = viewModel.cameraState.first()
        assertFalse(actualState.isLoading)
        assertFalse(actualState.isInitialized)
        assertEquals("Не удалось инициализировать камеру", actualState.error)
    }

    @Test
    fun `initializeCamera should set error state when exception occurs`() = runTest(testDispatcher) {
        // Given
        val exceptionMessage = "Camera not available"
        `when`(mockRepositoryFactory.create(mockLifecycleOwner)).thenThrow(RuntimeException(exceptionMessage))

        // When
        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // Then
        val actualState = viewModel.cameraState.first()
        assertFalse(actualState.isLoading)
        assertFalse(actualState.isInitialized)
        assertTrue(actualState.error?.contains(exceptionMessage) == true)
    }

    @Test
    fun `setZoom should call repository setZoom`() = runTest(testDispatcher) {
        // Given
        val zoomLevel = 2.5f
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.setZoom(zoomLevel)
        testScheduler.advanceUntilIdle()

        // Then
        verify(mockCameraRepository).setZoom(zoomLevel)
    }

    @Test
    fun `toggleFlash should call repository toggleFlash`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.toggleFlash()
        testScheduler.advanceUntilIdle()

        // Then
        verify(mockCameraRepository).toggleFlash()
    }

    @Test
    fun `pauseCamera should call repository pauseCamera`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.pauseCamera()
        testScheduler.advanceUntilIdle()

        // Then
        verify(mockCameraRepository).pauseCamera()
    }

    @Test
    fun `resumeCamera should call repository resumeCamera`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.resumeCamera()
        testScheduler.advanceUntilIdle()

        // Then
        verify(mockCameraRepository).resumeCamera()
    }

    @Test
    fun `setFrozen should call repository setFrozen`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.setFrozen(true)
        testScheduler.advanceUntilIdle()

        // Then
        verify(mockCameraRepository).setFrozen(true)
    }

    @Test
    fun `clearError should call repository clearError`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.clearError()
        testScheduler.advanceUntilIdle()

        // Then
        verify(mockCameraRepository).clearError()
    }

    @Test
    fun `captureFrame should return bitmap from repository`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.captureFrame()

        // Then
        verify(mockCameraRepository).captureFrame()
    }

    @Test
    fun `getPreviewView should return view from repository`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.getPreviewView()

        // Then
        verify(mockCameraRepository).getPreviewView()
    }

    @Test
    fun `forceRecreatePreview should call pause and resume with delay`() = runTest(testDispatcher) {
        // Given
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.forceRecreatePreview()
        testScheduler.advanceTimeBy(60) // Даем время для выполнения корутины

        // Then
        verify(mockCameraRepository).pauseCamera()
        verify(mockCameraRepository).resumeCamera()
    }

    @Test
    fun `onCleared should release repository`() = runTest(testDispatcher) {
        // Given
        // Инициализируем камеру, чтобы репозиторий был установлен
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(MutableStateFlow(CameraState()))
        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When - вызываем onCleared через рефлексию
        val onClearedMethod: Method = CameraViewModel::class.java
            .getDeclaredMethod("onCleared")
        onClearedMethod.isAccessible = true
        onClearedMethod.invoke(viewModel)

        // Then
        verify(mockCameraRepository).release()
    }

    @Test
    fun `initial state should have loading true`() {
        // When
        val initialState = viewModel.cameraState.value

        // Then
        assertTrue(initialState.isLoading)
        assertEquals(1.0f, initialState.zoomLevel)
        assertFalse(initialState.isFlashEnabled)
        assertFalse(initialState.isFrozen)
        assertNull(initialState.error)
    }

    @Test
    fun `repository methods should not be called when repository is null`() = runTest(testDispatcher) {
        // When - вызываем методы до инициализации камеры
        viewModel.setZoom(2.0f)
        viewModel.toggleFlash()
        viewModel.pauseCamera()
        viewModel.resumeCamera()
        viewModel.setFrozen(true)
        viewModel.clearError()
        testScheduler.advanceUntilIdle()

        // Then - не должно быть вызовов, так как репозиторий null
        verify(mockCameraRepository, never()).setZoom(any())
        verify(mockCameraRepository, never()).toggleFlash()
        verify(mockCameraRepository, never()).pauseCamera()
        verify(mockCameraRepository, never()).resumeCamera()
        verify(mockCameraRepository, never()).setFrozen(any())
        verify(mockCameraRepository, never()).clearError()
    }

    @Test
    fun `withRepository should not crash when repository is null`() = runTest(testDispatcher) {
        // Given - репозиторий не инициализирован

        // When - вызываем методы, которые используют withRepository
        viewModel.setZoom(1.5f)
        viewModel.toggleFlash()
        viewModel.pauseCamera()
        viewModel.resumeCamera()
        testScheduler.advanceUntilIdle()

        // Then - не должно быть исключений
        // verify(mockCameraRepository, never()) уже проверяется в других тестах
        assertTrue(true) // Просто проверяем, что не упало
    }

    @Test
    fun `camera state flow should emit updated values from repository`() = runTest(testDispatcher) {
        // Given
        val cameraStates = listOf(
            CameraState(isLoading = false, isInitialized = true, zoomLevel = 1.0f),
            CameraState(isLoading = false, isInitialized = true, zoomLevel = 2.0f),
            CameraState(isLoading = false, isInitialized = true, zoomLevel = 3.0f)
        )

        val stateFlow = MutableStateFlow(cameraStates[0])
        `when`(mockCameraRepository.initializeCamera()).thenReturn(true)
        `when`(mockCameraRepository.getCameraState()).thenReturn(stateFlow)

        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // When
        stateFlow.value = cameraStates[1]
        stateFlow.value = cameraStates[2]
        testScheduler.advanceUntilIdle()

        // Then
        assertEquals(3.0f, viewModel.cameraState.value.zoomLevel)
    }

    @Test
    fun `initializeCamera should handle null repository from factory`() = runTest(testDispatcher) {
        // Given
        `when`(mockRepositoryFactory.create(mockLifecycleOwner)).thenReturn(null)

        // When
        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // Then
        val actualState = viewModel.cameraState.first()
        assertFalse(actualState.isLoading)
        assertFalse(actualState.isInitialized)
        assertEquals("Не удалось инициализировать камеру", actualState.error)
    }

    @Test
    fun `initializeCamera should handle when repository is not CameraRepositoryImpl`() = runTest(testDispatcher) {
        // Given
        val otherRepository = mock(ru.wert.quickloupe.data.repository.CameraRepository::class.java)
        `when`(mockRepositoryFactory.create(mockLifecycleOwner)).thenReturn(otherRepository)

        // When
        viewModel.initializeCamera(mockLifecycleOwner)
        testScheduler.advanceUntilIdle()

        // Then
        val actualState = viewModel.cameraState.first()
        assertFalse(actualState.isLoading)
        assertFalse(actualState.isInitialized)
        assertEquals("Не удалось инициализировать камеру", actualState.error)
    }
}