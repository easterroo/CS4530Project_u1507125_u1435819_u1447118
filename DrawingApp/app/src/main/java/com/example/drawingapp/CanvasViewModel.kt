package com.example.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.plus

class CanvasViewModel : ViewModel() {
    //TODO: figure out how to pass drawing id and title
    private val _drawing = MutableStateFlow(Drawing(0, ""))
    private val _strokes = MutableStateFlow(listOf<Stroke>())
    private val _brushSize = MutableStateFlow(16f)
    private val _brushShape = MutableStateFlow(BrushShape.LINE)
    private val _currentColor = MutableStateFlow(Color.Red)

    val drawing : StateFlow<Drawing> = _drawing
    val strokes : StateFlow<List<Stroke>> = _strokes
    val brushSize : StateFlow<Float> = _brushSize
    val brushShape : StateFlow<BrushShape> = _brushShape
    val currentColor : StateFlow<Color> = _currentColor

    fun addStroke(currentStroke: Stroke) {
        _strokes.value += currentStroke
    }

    fun updateLastStroke(updatedStroke: Stroke) {
        val currentList = _strokes.value
        if (currentList.isNotEmpty()) {
            _strokes.value = currentList.dropLast(1) + updatedStroke
        }
    }

    fun setBrushSize(newBrushSize: Float) {
        _brushSize.value = newBrushSize
    }

    fun setBrushShape(newBrushShape: BrushShape) {
        _brushShape.value = newBrushShape
    }

    fun setColor(newColor: Color) {
        _currentColor.value = newColor
    }

    class Stroke {
        private val _stroke = MutableStateFlow(listOf<Offset>())
        private val _strokeShape = MutableStateFlow(BrushShape.LINE)
        private val _strokeSize = MutableStateFlow(16f)
        private val _strokeColor = MutableStateFlow(Color.Red)

        val stroke : StateFlow<List<Offset>> = _stroke
        val strokeShape : StateFlow<BrushShape> = _strokeShape
        val strokeSize : StateFlow<Float> = _strokeSize
        val strokeColor : StateFlow<Color> = _strokeColor

        fun setStroke(stroke: List<Offset>) {
            _stroke.value = stroke
        }

        fun setShape(shape: BrushShape) {
            _strokeShape.value = shape
        }

        fun setSize(size: Float) {
            _strokeSize.value = size
        }

        fun setColor(color: Color) {
            _strokeColor.value = color
        }
    }
}

enum class BrushShape{
    LINE, CIRCLE, SQUARE
}