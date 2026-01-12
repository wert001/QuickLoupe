package ru.wert.quickloupe.presentation.camera

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import io.mockk.*
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.wert.quickloupe.di.CameraRepositoryFactory
import ru.wert.quickloupe.data.repository.CameraRepositoryImpl
import ru.wert.quickloupe.domain.models.CameraState

/**
 * Модульные тесты для CameraViewModel.
 * Проверяет логику работы ViewModel для управления камерой.
 *
 * Используемые библиотеки:
 * - JUnit 5 Jupiter для структуры тестов
 * - MockK для мокинга зависимостей
 * - Kotlin Coroutines Test для тестирования асинхронного кода
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CameraViewModelTest {

    private lateinit var viewModel: CameraViewModel
    private lateinit var mockRepositoryFactory: CameraRepositoryFactory
    private lateinit var mockCameraRepository: CameraRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockLifecycleOwner = mockk<LifecycleOwner>(relaxed = true)

    private val testCameraState = CameraState(
        isFrozen = false,
        zoomLevel = 1.0f,
        isFlashEnabled = false,
        isLoading = false,
        error = null
    )

    /**
     * Настройка тестового окружения перед каждым тестом.
     * Инициализирует моки зависимостей и создает экземпляр ViewModel.
     */
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockCameraRepository = mockk(relaxed = true)
        mockRepositoryFactory = mockk<CameraRepositoryFactory>()

        // Настраиваем factory для создания мок-репозитория
        coEvery { mockRepositoryFactory.create(any()) } returns mockCameraRepository
        coEvery { mockCameraRepository.initializeCamera() } returns true
        every { mockCameraRepository.getCameraState() } returns MutableStateFlow(testCameraState)

        viewModel = CameraViewModel(mockRepositoryFactory)

        // Инициализируем камеру перед тестами
        viewModel.initializeCamera(mockLifecycleOwner)
        testScope.advanceUntilIdle()
    }

    /**
     * Очистка тестового окружения после каждого теста.
     * Сбрасывает основной диспетчер корутин и очищает моки.
     */
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * Тест успешной инициализации камеры.
     * Проверяет, что при успешной инициализации состояние камеры обновляется корректно.
     */
    @Test
    fun `инициализация камеры должна успешно выполниться и начать сбор состояния`() = testScope.runTest {
        // Подготовка
        val testStateFlow = MutableStateFlow(testCameraState)
        every { mockCameraRepository.getCameraState() } returns testStateFlow

        // Действие (уже выполнено в setUp)
        advanceUntilIdle()

        // Проверка
        coVerify { mockRepositoryFactory.create(mockLifecycleOwner) }
        coVerify { mockCameraRepository.initializeCamera() }

        // Проверяем, что состояние обновилось
        val state = viewModel.cameraState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    /**
     * Тест обработки неудачной инициализации камеры.
     * Проверяет, что при неудачной инициализации устанавливается сообщение об ошибке.
     */
    @Test
    fun `инициализация камеры должна обрабатывать неудачную инициализацию`() = testScope.runTest {
        // Подготовка
        // Создаем новый viewModel для этого теста
        val testViewModel = CameraViewModel(mockRepositoryFactory)
        coEvery { mockCameraRepository.initializeCamera() } returns false

        // Действие
        testViewModel.initializeCamera(mockLifecycleOwner)
        advanceUntilIdle()

        // Проверка
        val state = testViewModel.cameraState.value
        assertFalse(state.isLoading)
        assertEquals("Не удалось инициализировать камеру", state.error)
    }

    /**
     * Тест обработки исключения при инициализации камеры.
     * Проверяет, что исключения корректно обрабатываются и сохраняются в состоянии ошибки.
     */
    @Test
    fun `инициализация камеры должна обрабатывать исключения`() = testScope.runTest {
        // Подготовка
        // Создаем новый viewModel для этого теста
        val testViewModel = CameraViewModel(mockRepositoryFactory)
        val exceptionMessage = "Тестовое исключение"
        coEvery { mockRepositoryFactory.create(any()) } throws RuntimeException(exceptionMessage)

        // Действие
        testViewModel.initializeCamera(mockLifecycleOwner)
        advanceUntilIdle()

        // Проверка
        val state = testViewModel.cameraState.value
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains(exceptionMessage) == true)
    }

    /**
     * Тест установки уровня зума.
     * Проверяет, что вызов setZoom делегируется в репозиторий.
     */
    @Test
    fun `установка зума должна вызывать соответствующий метод репозитория`() = testScope.runTest {
        // Подготовка
        val zoomLevel = 2.5f
        coEvery { mockCameraRepository.setZoom(zoomLevel) } returns Unit

        // Действие
        viewModel.setZoom(zoomLevel)
        advanceUntilIdle()

        // Проверка
        coVerify { mockCameraRepository.setZoom(zoomLevel) }
    }

    /**
     * Тест переключения вспышки.
     * Проверяет, что вызов toggleFlash делегируется в репозиторий.
     */
    @Test
    fun `переключение вспышки должно вызывать соответствующий метод репозитория`() = testScope.runTest {
        // Подготовка
        coEvery { mockCameraRepository.toggleFlash() } returns true

        // Действие
        viewModel.toggleFlash()
        advanceUntilIdle()

        // Проверка
        coVerify { mockCameraRepository.toggleFlash() }
    }

    /**
     * Тест приостановки работы камеры.
     * Проверяет, что вызов pauseCamera делегируется в репозиторий.
     */
    @Test
    fun `приостановка камеры должна вызывать соответствующий метод репозитория`() = testScope.runTest {
        // Подготовка
        coEvery { mockCameraRepository.pauseCamera() } returns Unit

        // Действие
        viewModel.pauseCamera()
        advanceUntilIdle()

        // Проверка
        coVerify { mockCameraRepository.pauseCamera() }
    }

    /**
     * Тест возобновления работы камеры.
     * Проверяет, что вызов resumeCamera делегируется в репозиторий.
     */
    @Test
    fun `возобновление камеры должно вызывать соответствующий метод репозитория`() = testScope.runTest {
        // Подготовка
        coEvery { mockCameraRepository.resumeCamera() } returns Unit

        // Действие
        viewModel.resumeCamera()
        advanceUntilIdle()

        // Проверка
        coVerify { mockCameraRepository.resumeCamera() }
    }

    /**
     * Тест захвата кадра.
     * Проверяет, что вызов captureFrame делегируется в репозиторий и возвращает результат.
     */
    @Test
    fun `захват кадра должен вызывать соответствующий метод репозитория`() {
        // Подготовка
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        every { mockCameraRepository.captureFrame() } returns mockBitmap

        // Действие
        val result = viewModel.captureFrame()

        // Проверка
        verify { mockCameraRepository.captureFrame() }
        assertEquals(mockBitmap, result)
    }

    /**
     * Тест захвата кадра, когда репозиторий возвращает null.
     * Проверяет, что метод корректно обрабатывает null-результат.
     */
    @Test
    fun `захват кадра должен возвращать null когда репозиторий возвращает null`() {
        // Подготовка
        every { mockCameraRepository.captureFrame() } returns null

        // Действие
        val result = viewModel.captureFrame()

        // Проверка
        verify { mockCameraRepository.captureFrame() }
        assertNull(result)
    }

    /**
     * Тест заморозки/разморозки камеры.
     * Проверяет, что вызов setFrozen делегируется в репозиторий.
     */
    @Test
    fun `установка состояния заморозки должна вызывать соответствующий метод репозитория`() {
        // Подготовка
        val frozen = true
        every { mockCameraRepository.setFrozen(frozen) } just Runs

        // Действие
        viewModel.setFrozen(frozen)

        // Проверка
        verify { mockCameraRepository.setFrozen(frozen) }
    }

    /**
     * Тест очистки ошибки.
     * Проверяет, что вызов clearError делегируется в репозиторий.
     */
    @Test
    fun `очистка ошибки должна вызывать соответствующий метод репозитория`() {
        // Подготовка
        every { mockCameraRepository.clearError() } just Runs

        // Действие
        viewModel.clearError()

        // Проверка
        verify { mockCameraRepository.clearError() }
    }

    /**
     * Тест получения preview-вью.
     * Проверяет, что вызов getPreviewView делегируется в репозиторий.
     */
    @Test
    fun `получение preview-вью должно вызывать соответствующий метод репозитория`() {
        // Подготовка
        val mockPreviewView = mockk<PreviewView>(relaxed = true)
        every { mockCameraRepository.getPreviewView() } returns mockPreviewView

        // Действие
        val result = viewModel.getPreviewView()

        // Проверка
        verify { mockCameraRepository.getPreviewView() }
        assertEquals(mockPreviewView, result)
    }

    /**
     * Тест получения preview-вью, когда репозиторий не инициализирован.
     * Проверяет, что метод возвращает null при отсутствии репозитория.
     */
    @Test
    fun `получение preview-вью должно возвращать null когда репозиторий не инициализирован`() {
        // Подготовка
        // Создаем новый viewModel без вызова initializeCamera
        val testViewModel = CameraViewModel(mockRepositoryFactory)
        // Не вызываем initializeCamera, поэтому repository останется null

        // Действие
        val result = testViewModel.getPreviewView()

        // Проверка
        assertNull(result)
    }

    /**
     * Тест освобождения ресурсов при очистке ViewModel.
     * Проверяет, что при вызове onCleared() освобождаются ресурсы репозитория.
     */
    @Test
    fun `освобождение ресурсов должно вызывать соответствующий метод репозитория при очистке ViewModel`() {
        // Подготовка
        every { mockCameraRepository.release() } just Runs

        // Действие - используем рефлексию для вызова protected метода
        viewModel.run {
            val onClearedMethod = CameraViewModel::class.java
                .getDeclaredMethod("onCleared")
            onClearedMethod.isAccessible = true
            onClearedMethod.invoke(this)
        }

        // Проверка
        verify { mockCameraRepository.release() }
    }

    /**
     * Тест принудительного пересоздания preview.
     * Проверяет, что метод приостанавливает и возобновляет работу камеры.
     */
    @Test
    fun `принудительное пересоздание preview должно приостанавливать и возобновлять камеру`() = testScope.runTest {
        // Подготовка
        coEvery { mockCameraRepository.pauseCamera() } returns Unit
        coEvery { mockCameraRepository.resumeCamera() } returns Unit

        // Действие
        viewModel.forceRecreatePreview()
        advanceUntilIdle()

        // Проверка
        coVerify { mockCameraRepository.pauseCamera() }
        coVerify(exactly = 1) { mockCameraRepository.resumeCamera() }
    }

    /**
     * Тест начального состояния камеры.
     * Проверяет, что ViewModel инициализируется с правильным начальным состоянием.
     */
    @Test
    fun `начальное состояние камеры должно быть в режиме загрузки`() {
        // Создаем новый viewModel без инициализации для проверки начального состояния
        val testViewModel = CameraViewModel(mockRepositoryFactory)

        // Действие
        val initialState = testViewModel.cameraState.value

        // Проверка
        assertTrue(initialState.isLoading)
        assertNull(initialState.error)
        assertEquals(1.0f, initialState.zoomLevel)
        assertFalse(initialState.isFrozen)
        assertFalse(initialState.isFlashEnabled)
    }

    /**
     * Тест работы методов при отсутствии инициализированного репозитория.
     * Проверяет, что методы не вызывают исключений при null-репозитории.
     */
    @Test
    fun `операции не должны вызывать исключения когда репозиторий не инициализирован`() = testScope.runTest {
        // Подготовка
        val testViewModel = CameraViewModel(mockRepositoryFactory)
        // Не вызываем initializeCamera, поэтому repository останется null

        // Действие & Проверка (не должно быть исключений)
        testViewModel.setZoom(2.0f)
        testViewModel.toggleFlash()
        testViewModel.pauseCamera()
        testViewModel.resumeCamera()
        testViewModel.setFrozen(true)
        testViewModel.clearError()
        testViewModel.captureFrame()
        testViewModel.forceRecreatePreview()

        advanceUntilIdle()

        // Все операции должны завершиться без ошибок
        assertTrue(true)
    }

    /**
     * Тест обновления состояния камеры при изменении в репозитории.
     * Проверяет, что ViewModel корректно реагирует на изменения состояния.
     */
    @Test
    fun `состояние камеры должно обновляться при изменении состояния в репозитории`() = testScope.runTest {
        // Подготовка
        val testViewModel = CameraViewModel(mockRepositoryFactory)
        val testStateFlow = MutableStateFlow(testCameraState)
        every { mockCameraRepository.getCameraState() } returns testStateFlow

        testViewModel.initializeCamera(mockLifecycleOwner)
        advanceUntilIdle()

        val newState = testCameraState.copy(
            zoomLevel = 2.5f,
            isFlashEnabled = true,
            isFrozen = true
        )

        // Действие
        testStateFlow.value = newState
        advanceUntilIdle()

        // Проверка
        val currentState = testViewModel.cameraState.value
        assertEquals(2.5f, currentState.zoomLevel)
        assertTrue(currentState.isFlashEnabled)
        assertTrue(currentState.isFrozen)
    }

    /**
     * Тест graceful обработки null-репозитория в методе withRepository.
     * Проверяет, что внутренний метод корректно обрабатывает отсутствие репозитория.
     */
    @Test
    fun `метод withRepository должен корректно обрабатывать null-репозиторий`() = testScope.runTest {
        // Подготовка
        val testViewModel = CameraViewModel(mockRepositoryFactory)
        // Не вызываем initializeCamera, поэтому repository останется null

        // Действие - вызываем метод, который использует withRepository
        testViewModel.setZoom(2.0f)
        advanceUntilIdle()

        // Проверка - не должно быть исключений
        assertTrue(true)
    }
}