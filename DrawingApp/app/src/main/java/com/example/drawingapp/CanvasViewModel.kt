package com.example.drawingapp

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel representing the Canvas and the information held in it, including brush size, stroke, and color information
 */
class CanvasViewModel : ViewModel() {
    private val _drawing = MutableStateFlow(Drawing())
    val drawing: StateFlow<Drawing> = _drawing.asStateFlow()

    private val _brushSize = MutableStateFlow(16f)
    private val _brushShape = MutableStateFlow(BrushShape.LINE)
    private val _currentColor = MutableStateFlow(Color.Red)

    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()
    val brushShape: StateFlow<BrushShape> = _brushShape.asStateFlow()
    val currentColor: StateFlow<Color> = _currentColor.asStateFlow()

    /** Called when a stroke is drawn */
    fun addStroke(currentStroke: Stroke) {
        _drawing.update { currentDrawing ->
            currentDrawing.copy(strokes = currentDrawing.strokes + currentStroke)
        }
    }

    /** Updates last stroke when dragged in drawing */
    fun updateLastStroke(updatedStroke: Stroke) {
        _drawing.update { currentDrawing ->
            if (currentDrawing.strokes.isNotEmpty()) {
                currentDrawing.copy(
                    strokes = currentDrawing.strokes.dropLast(1) + updatedStroke
                )
            } else {
                currentDrawing
            }
        }
    }

    /** Changes the brush size upon selection */
    fun setBrushSize(newBrushSize: Float) {
        _brushSize.value = newBrushSize
    }

    /** Changes the brush shape upon selection */
    fun setBrushShape(newBrushShape: BrushShape) {
        _brushShape.value = newBrushShape
    }

    /** Changes the brush color upon selection */
    fun setColor(newColor: Color) {
        _currentColor.value = newColor
    }

    /** Changes and updates the drawing title */
    fun updateTitle(newTitle: String) {
        _drawing.update { currentDrawing ->
            currentDrawing.copy(title = newTitle)
        }
    }

    /** Sets the canvas size; Used in opening the canvas compared to thumbnail */
    fun setCanvasSize(width: Float, height: Float) {
        _drawing.update { current ->
            if (current.canvasWidth <= 1f || current.canvasHeight <= 1f) {
                current.copy(canvasWidth = width, canvasHeight = height)
            } else {
                current
            }
        }
    }

    /** Opens the correct drawing with the saved strokes in the CanvasScreen */
    fun openDrawing(drawing: Drawing?) {
        _drawing.update { current ->
            drawing ?: Drawing(canvasWidth = current.canvasWidth, canvasHeight = current.canvasHeight)
        }
    }

    /** Sets the ID for the canvas's drawing; TODO: Change with Room storage implementation */
    fun setId(id: Int) {
        _drawing.update {it.copy(id = id)}
    }
}

