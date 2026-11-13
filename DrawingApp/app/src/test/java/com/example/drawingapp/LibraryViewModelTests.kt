package com.example.drawingapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class FakeDrawingDAO : DrawingDAO {
    private val drawings = mutableListOf<DrawingData>()
    private val _flow = MutableStateFlow<List<DrawingData>>(emptyList())

    private fun emit() {
        _flow.value = drawings.toList()
    }

    override suspend fun addDrawing(drawing: DrawingData): Long {
        val nextId = if (drawing.id == 0) (drawings.maxOfOrNull { it.id } ?: 0) + 1 else drawing.id
        drawings.removeAll { it.id == nextId }
        drawings.add(drawing.copy(id = nextId))
        emit()
        return nextId.toLong()
    }

    override suspend fun removeDrawing(id: Int) {
        drawings.removeAll { it.id == id }
        emit()
    }

    override suspend fun updateTitle(title: String, id: Int) {
        val index = drawings.indexOfFirst { it.id == id }
        if (index != -1) {
            drawings[index] = drawings[index].copy(title = title)
            emit()
        }
    }

    override suspend fun updateWidth(width: Float, id: Int) {
        val index = drawings.indexOfFirst { it.id == id }
        if (index != -1) {
            drawings[index] = drawings[index].copy(width = width)
            emit()
        }
    }

    override suspend fun updateHeight(height: Float, id: Int) {
        val index = drawings.indexOfFirst { it.id == id }
        if (index != -1) {
            drawings[index] = drawings[index].copy(height = height)
            emit()
        }
    }

    override suspend fun updateFilePath(filePath: String, id: Int) {
        val index = drawings.indexOfFirst { it.id == id }
        if (index != -1) {
            drawings[index] = drawings[index].copy(filePath = filePath)
            emit()
        }
    }

    override suspend fun getDrawing(id: Int): DrawingData? {
        return drawings.find { it.id == id }
    }

    override fun allDrawings(): Flow<List<DrawingData>> {
        return _flow
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTests {
    private lateinit var repo: DrawingRepository
    private lateinit var vm: LibraryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        repo = DrawingRepository(CoroutineScope(Dispatchers.Unconfined), FakeDrawingDAO())
        vm = LibraryViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getDrawing_unknownId_returnsDefault() {
        val drawing = vm.getDrawing(99)
        assertNotNull(drawing)
        assertEquals(99, drawing.id)
    }

    @Test
    fun getDrawing_knownId_returnsCorrect() {
        val data = DrawingData(id = 1, title = "A")
        val data2 = DrawingData(id = 2, title = "B")
        val data3 = DrawingData(id = 3, title = "C")

        runBlocking {
            vm.saveDrawing(data = data, drawing = DrawingCanvas(), context = null)
            vm.saveDrawing(data = data2, drawing = DrawingCanvas(), context = null)
            vm.saveDrawing(data = data3, drawing = DrawingCanvas(), context = null)
        }

        val fetched = vm.getDrawing(2)
        assertNotNull(fetched)
        assertEquals("B", fetched.title)
        assertEquals(2, fetched.id)
    }

    @Test
    fun saveAndGetDrawing() {
        val data = DrawingData(id = 1, title = "A")
        vm.saveDrawing(data = data, drawing = DrawingCanvas(), context = null)
        val fetched = vm.getDrawing(1)
        assertNotNull(fetched)
        assertEquals("A", fetched.title)
        assertEquals(1, fetched.id)
    }

    @Test
    fun deleteDrawing_drawingRemoved() {
        val data = DrawingData(id = 1, title = "To Be Deleted")

        runBlocking {
            vm.saveDrawing(data = data, drawing = DrawingCanvas(), context = null)
            vm.deleteDrawing(id = 1, context = null)
        }

        assertEquals(0, vm.drawingsSnapshots.value.size)
    }

    @Test
    fun saveDrawing_updatesExistingDrawing() {
        val initial = DrawingData(id = 1, title = "Old Title")
        runBlocking {
            vm.saveDrawing(data = initial, drawing = DrawingCanvas(width = 500f, height = 500f), context = null)
        }

        val updated = DrawingData(id = 1, title = "New Title")
        runBlocking {
            vm.saveDrawing(data = updated, drawing = DrawingCanvas(width = 800f, height = 800f), context = null)
        }

        assertEquals("New Title", vm.getDrawing(1).title)
        assertEquals(800f, vm.getDrawing(1).width)
    }
}


