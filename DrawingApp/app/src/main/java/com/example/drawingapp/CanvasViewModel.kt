package com.example.drawingapp

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel representing the Canvas and the information held in it, including brush size, stroke, and color information
 */
class CanvasViewModel(private val repository: DrawingRepository) : ViewModel() {
    private val _drawing = MutableStateFlow(DrawingCanvas())
    private val _image = MutableStateFlow<ImageBitmap?>(null)
    private val _id = MutableStateFlow(0)
    private val _title = MutableStateFlow("Untitled Drawing")
    private val _brushSize = MutableStateFlow(16f)
    private val _brushShape = MutableStateFlow(BrushShape.LINE)
    private val _currentColor = MutableStateFlow(Color.Red)

    val drawing: StateFlow<DrawingCanvas> = _drawing.asStateFlow()
    val image: StateFlow<ImageBitmap?> = _image.asStateFlow()
    val id: StateFlow<Int> = _id.asStateFlow()
    val title: StateFlow<String> = _title.asStateFlow()
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
    fun updateTitle(newTitle: String, id: Int = _id.value) {
        _title.value = newTitle
        if (id != 0) {
            repository.updateTitle(newTitle, id)
        }
    }

    /** Sets the canvas size; Used in opening the canvas compared to thumbnail */
    fun setCanvasSize(width: Float, height: Float) {
        _drawing.update { current ->
            if (current.width <= 1f || current.height <= 1f) {
                current.copy(width = width, height = height)
            } else {
                current
            }
        }
    }

    /** Opens the correct drawing with the saved strokes in the CanvasScreen */
    fun openDrawing(
        image: ImageBitmap?,
        id: Int,
        title: String = "Untitled Drawing",
        width: Float = 1f,
        height: Float = 1f
    ) {
        _image.value = image
        _id.value = id
        _title.value = title
        val initialWidth = when {
            width > 1f -> width
            image != null -> image.width.toFloat()
            else -> 1f
        }
        val initialHeight = when {
            height > 1f -> height
            image != null -> image.height.toFloat()
            else -> 1f
        }
        _drawing.value = DrawingCanvas(width = initialWidth, height = initialHeight)
    }

    fun newDrawing() {
        _drawing.value = DrawingCanvas()
        _image.value = null
        _id.value = 0
        _title.value = "Untitled Drawing"
    }
}

