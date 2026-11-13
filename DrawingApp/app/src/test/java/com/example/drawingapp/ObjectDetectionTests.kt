package com.example.drawingapp

import com.example.drawingapp.objdetection.BoundingBox
import com.example.drawingapp.objdetection.DetectedObject
import com.example.drawingapp.objdetection.ObjectDetectionRepository
import com.example.drawingapp.objdetection.ObjectDetectionViewModel
import com.example.drawingapp.objdetection.VisionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class FakeObjectDetectionRepository : ObjectDetectionRepository() {
    var result: Result<List<DetectedObject>> = Result.success(emptyList())

    override suspend fun detectObjectsInImage(imageFile: File): Result<List<DetectedObject>> {
        return result
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectDetectionTests {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeObjectDetectionRepository
    private lateinit var myVM: ObjectDetectionViewModel
    private lateinit var tempFile: File

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        fakeRepo = FakeObjectDetectionRepository()
        myVM = ObjectDetectionViewModel(fakeRepo)
        tempFile = File.createTempFile("test", ".png")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        if (tempFile.exists())
            tempFile.delete()
    }

    @Test
    fun detectObjects_emptyResult_handlesGracefully() = runTest(dispatcher) {
        fakeRepo.result = Result.success(emptyList())
        myVM.detectObjects(tempFile.absolutePath)
        dispatcher.scheduler.advanceUntilIdle()

        val state = myVM.uiState.value
        assertTrue(state is VisionUiState.Success)
        val successState = state as VisionUiState.Success
        assertTrue(successState.detectedObjects.isEmpty())
    }

    @Test
    fun detectObjects_success_updatesUiState() = runTest(dispatcher) {
        val mockDetections = listOf(
            DetectedObject(
                label = "Cat",
                confidenceScore = 98.5f,
                boundingBox = BoundingBox(100, 200, 300, 400)
            )
        )
        fakeRepo.result = Result.success(mockDetections)
        assertEquals(VisionUiState.Idle, myVM.uiState.value)
        myVM.detectObjects(tempFile.absolutePath)
        dispatcher.scheduler.advanceUntilIdle()

        val state = myVM.uiState.value
        assertTrue(state is VisionUiState.Success)
        val successState = state as VisionUiState.Success
        assertEquals(1, successState.detectedObjects.size)
        assertEquals("Cat", successState.detectedObjects[0].label)
        assertEquals(98.5f, successState.detectedObjects[0].confidenceScore)
    }

    @Test
    fun detectObjects_failure_updatesUiState() = runTest(dispatcher) {
        val errorMessage = "Network timeout or API quota exceeded"
        fakeRepo.result = Result.failure(Exception(errorMessage))
        myVM.detectObjects(tempFile.absolutePath)
        dispatcher.scheduler.advanceUntilIdle()

        val state = myVM.uiState.value
        assertTrue(state is VisionUiState.Error)
        val errorState = state as VisionUiState.Error
        assertEquals(errorMessage, errorState.message)
    }
}
