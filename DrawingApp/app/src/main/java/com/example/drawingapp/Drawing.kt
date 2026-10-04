package com.example.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Holds the identifying information of the drawing
 */
data class Drawing (
    val id: Int = 0,
    val title: String = "Untitled Drawing",
    val strokes: List<Stroke> = listOf(),
    val canvasWidth: Float = 1f,
    val canvasHeight: Float = 1f) {

}

/**
 * Holds information about the actual strokes used in the drawing
 */
data class Stroke(
    val stroke: List<Offset>,
    val shape: BrushShape,
    val size: Float,
    val color: Color
)

/**
 * Enum to differentiate brush shapes for drawing
 */
enum class BrushShape{
    LINE, CIRCLE, SQUARE
}

/**
 * Redraws the strokes for the drawing as saved
 */
fun DrawScope.drawStrokes(strokes: List<Stroke>) {
    strokes.forEach {
        val color = it.color
        val size = it.size
        val strokeList = it.stroke

        when (it.shape) {
            BrushShape.LINE -> {
                for (i in 0 until strokeList.size - 1) {
                    drawLine(
                        color = color,
                        strokeList[i], strokeList[i + 1], size,
                        blendMode = if (color == Color.Transparent) BlendMode.Clear
                        else BlendMode.SrcOver
                    )
                }
            }

            BrushShape.CIRCLE -> {
                strokeList.forEach { point ->
                    drawCircle(
                        color = color,
                        size / 2, point,
                        blendMode = if (color == Color.Transparent) BlendMode.Clear
                        else BlendMode.SrcOver
                    )
                }
            }

            BrushShape.SQUARE -> {
                strokeList.forEach { point ->
                    drawRect(
                        color = color,
                        Offset(point.x - size / 2, point.y - size / 2),
                        Size(size, size),
                        blendMode = if (color == Color.Transparent) BlendMode.Clear
                        else BlendMode.SrcOver
                    )
                }
            }
        }
    }
}