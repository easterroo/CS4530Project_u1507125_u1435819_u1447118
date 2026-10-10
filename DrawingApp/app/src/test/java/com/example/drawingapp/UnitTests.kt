package com.example.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class LibraryViewModelTest {
    private lateinit var vm: LibraryViewModel

    @Before
    fun setup() { vm = LibraryViewModel() }

    @Test
    fun saveDrawing_withIdZero_assignsNewId() {
        val saved = vm.saveDrawing(Drawing(title = "A"))
        assertEquals(1, saved.id)
        assertEquals(1, vm.drawingsSnapshots.value.size)
    }

    @Test
    fun saveDrawing_assignsIncrementingIds() {
        val a = vm.saveDrawing(Drawing(title = "A"))
        val b = vm.saveDrawing(Drawing(title = "B"))
        assertNotEquals(a.id, b.id)
    }

    @Test
    fun saveDrawing_existingId_updatesInsteadOfAdding() {
        val saved = vm.saveDrawing(Drawing(title = "Old"))
        vm.saveDrawing(saved.copy(title = "New"))
        assertEquals(1, vm.drawingsSnapshots.value.size)
        assertEquals("New", vm.getDrawing(saved.id)?.title)
    }

    @Test
    fun getDrawing_unknownId_returnsNull() {
        assertNull(vm.getDrawing(99))
    }

    @Test
    fun deleteDrawing_removesIt() {
        val saved = vm.saveDrawing(Drawing())
        vm.deleteDrawing(saved)
        assertTrue(vm.drawingsSnapshots.value.isEmpty())
    }
}

class CanvasViewModelTest {
    private lateinit var vm: CanvasViewModel
    private fun stroke(pts: List<Offset>, color: Color = Color.Red) =
        Stroke(pts, BrushShape.LINE, 16f, color)

    @Before
    fun setup() { vm = CanvasViewModel() }

    @Test
    fun defaults() {
        assertEquals(16f, vm.brushSize.value)
        assertEquals(BrushShape.LINE, vm.brushShape.value)
        assertEquals(Color.Red, vm.currentColor.value)
        assertEquals("Untitled Drawing", vm.drawing.value.title)
    }

    @Test
    fun addStroke_appends() {
        vm.addStroke(stroke(listOf(Offset(0f, 0f))))
        vm.addStroke(stroke(listOf(Offset(1f, 1f))))
        assertEquals(2, vm.drawing.value.strokes.size)
    }

    @Test
    fun updateLastStroke_replacesOnlyLast() {
        val first = stroke(listOf(Offset(0f, 0f)))
        vm.addStroke(first)
        vm.addStroke(stroke(listOf(Offset(1f, 1f))))
        val updated = stroke(listOf(Offset(1f, 1f), (Offset(2f, 2f))))
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
        assertEquals(800f, vm.drawing.value.canvasWidth)
        assertEquals(1200f, vm.drawing.value.canvasHeight)
    }

    @Test
    fun openDrawing_null_createsBlankKeepingCanvasSize() {
        vm.setCanvasSize(800f, 1200f)
        vm.addStroke(stroke(listOf(Offset.Zero)))
        vm.openDrawing(null)
        assertTrue(vm.drawing.value.strokes.isEmpty())
        assertEquals(800f, vm.drawing.value.canvasWidth)
    }

    @Test
    fun openDrawing_loadsGivenDrawing() {
        val d = Drawing(id = 5, title = "Saved", canvasWidth = 500f, canvasHeight = 500f)
        vm.openDrawing(d)
        assertEquals(d, vm.drawing.value)
    }

    @Test
    fun setters_updateState() {
        vm.setBrushSize(32f); vm.setBrushShape(BrushShape.SQUARE)
        vm.setColor(Color.Blue); vm.updateTitle("Hi"); vm.setId(7)
        assertEquals(32f, vm.brushSize.value)
        assertEquals(BrushShape.SQUARE, vm.brushShape.value)
        assertEquals(Color.Blue, vm.currentColor.value)
        assertEquals("Hi", vm.drawing.value.title)
        assertEquals(7, vm.drawing.value.id)
    }
}