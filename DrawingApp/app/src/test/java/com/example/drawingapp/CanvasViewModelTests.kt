package com.example.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CanvasViewModelTests {
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
        vm.newDrawing()
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