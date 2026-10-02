package com.example.drawingapp

import android.content.Context
import android.util.JsonWriter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class CanvasViewModel : ViewModel() {
    private val _drawing = MutableStateFlow(Drawing())
    val drawing: StateFlow<Drawing> = _drawing.asStateFlow()

    private val _brushSize = MutableStateFlow(16f)
    private val _brushShape = MutableStateFlow(BrushShape.LINE)
    private val _currentColor = MutableStateFlow(Color.Red)

    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()
    val brushShape: StateFlow<BrushShape> = _brushShape.asStateFlow()
    val currentColor: StateFlow<Color> = _currentColor.asStateFlow()

    fun addStroke(currentStroke: Stroke) {
        _drawing.update { currentDrawing ->
            currentDrawing.copy(strokes = currentDrawing.strokes + currentStroke)
        }
    }

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

    fun setBrushSize(newBrushSize: Float) {
        _brushSize.value = newBrushSize
    }

    fun setBrushShape(newBrushShape: BrushShape) {
        _brushShape.value = newBrushShape
    }

    fun setColor(newColor: Color) {
        _currentColor.value = newColor
    }

    fun updateTitle(newTitle: String) {
        _drawing.update { currentDrawing ->
            currentDrawing.copy(title = newTitle)
        }
    }


    fun saveCanvas() {
        val currentDrawing = _drawing.value
        //launch scope {
//            if (currentDrawing.id == 0)
//                //Insert current drawing into db
//            else
                //update currentDrawing in db
//        }
    }
}

