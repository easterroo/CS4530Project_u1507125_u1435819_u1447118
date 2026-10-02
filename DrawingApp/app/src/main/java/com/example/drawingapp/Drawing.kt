package com.example.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Drawing (val id: Int = 0, val title: String = "Untitled Drawing",
                    val strokes: List<Stroke> = listOf()) {

}

data class Stroke(
    val stroke: List<Offset>,
    val shape: BrushShape,
    val size: Float,
    val color: Color
)

enum class BrushShape{
    LINE, CIRCLE, SQUARE
}