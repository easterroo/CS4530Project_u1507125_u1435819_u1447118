package com.example.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
class LibraryViewModelTest {
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

@OptIn(ExperimentalCoroutinesApi::class)
class CanvasViewModelTest {
    private lateinit var repo: DrawingRepository
    private lateinit var vm: CanvasViewModel

    private fun stroke(pts: List<Offset>, color: Color = Color.Red) =
        Stroke(pts, BrushShape.LINE, 16f, color)

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        repo = DrawingRepository(CoroutineScope(Dispatchers.Unconfined), FakeDrawingDAO())
        vm = CanvasViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaults() {
        assertEquals(16f, vm.brushSize.value)
        assertEquals(BrushShape.LINE, vm.brushShape.value)
        assertEquals(Color.Red, vm.currentColor.value)
        assertTrue(vm.drawing.value.strokes.isEmpty())
    }

    @Test
    fun addStroke_appends() {
        vm.addStroke(stroke(listOf(Offset(0f, 0f))))
        vm.addStroke(stroke(listOf(Offset(1f, 1f))))
        assertEquals(2, vm.drawing.value.strokes.size)
    }

    @Test
    fun eraserStroke_appends() {
        val eraserStroke = stroke(listOf(Offset(0f, 0f)), color = Color.Transparent)
        vm.addStroke(eraserStroke)
        vm.addStroke(eraserStroke)
        assertEquals(2, vm.drawing.value.strokes.size)
        assertEquals(Color.Transparent, vm.drawing.value.strokes[0].color)
    }

    @Test
    fun updateLastStroke_replacesOnlyLast() {
        val first = stroke(listOf(Offset(0f, 0f)))
        vm.addStroke(first)
        vm.addStroke(stroke(listOf(Offset(1f, 1f))))
        val updated = stroke(listOf(Offset(1f, 1f), Offset(2f, 2f)))
        vm.updateLastStroke(updated)
        assertEquals(first, vm.drawing.value.strokes[0])
        assertEquals(updated, vm.drawing.value.strokes[1])
    }

    @Test
    fun updateLastStroke_onEmpty_doesNothing() {
        vm.updateLastStroke(stroke(listOf(Offset.Zero)))
        assertTrue(vm.drawing.value.strokes.isEmpty())
    }

    @Test
    fun setCanvasSize_onlyAppliesFirstTime() {
        vm.setCanvasSize(800f, 1200f)
        vm.setCanvasSize(100f, 100f)
        assertEquals(1000f, vm.drawing.value.width)
        assertEquals(1000f, vm.drawing.value.height)
    }

    @Test
    fun openDrawing_resetsStrokesAndSetsId() {
        vm.setCanvasSize(800f, 1200f)
        vm.addStroke(stroke(listOf(Offset.Zero)))
        vm.openDrawing(null, 5, "My Test Drawing")
        assertTrue(vm.drawing.value.strokes.isEmpty())
        assertEquals(5, vm.id.value)
        assertEquals("My Test Drawing", vm.title.value)
    }

    @Test
    fun setters_updateState() {
        vm.setBrushSize(32f)
        vm.setBrushShape(BrushShape.SQUARE)
        vm.setColor(Color.Blue)
        assertEquals(32f, vm.brushSize.value)
        assertEquals(BrushShape.SQUARE, vm.brushShape.value)
        assertEquals(Color.Blue, vm.currentColor.value)
    }

    @Test
    fun updateTitle_updatesState() {
        vm.openDrawing(null, 5)
        vm.updateTitle("New Art")
        assertEquals("New Art", vm.title.value)
    }

    @Test
    fun newDrawing_resetsAllState() {
        vm.addStroke(stroke(listOf(Offset.Zero)))
        vm.updateTitle("Something")
        vm.newDrawing()
        
        assertTrue(vm.drawing.value.strokes.isEmpty())
        assertEquals(0, vm.id.value)
        assertNull(vm.image.value)
        assertEquals("Untitled Drawing", vm.title.value)
    }

    @Test
    fun newDrawing_updatesDatabase() {
        vm.newDrawing()
        assertEquals(0, vm.id.value)
    }

    @Test
    fun openDrawing_updatesDatabase() {
        vm.openDrawing(null, 5)
        assertEquals(5, vm.id.value)
    }
}
