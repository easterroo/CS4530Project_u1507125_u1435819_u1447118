package com.example.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Holds the identifying information of the drawing
 */
data class DrawingCanvas (
    val strokes: List<Stroke> = listOf(),
    val width: Float = 1f,
    val height: Float = 1f,
) {

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
                if (strokeList.size == 1) {
                    drawCircle(
                        color = color,
                        radius = size / 2,
                        center = strokeList[0],
                        blendMode = if (color == Color.Transparent) BlendMode.Clear else BlendMode.SrcOver
                    )
                } else {
                    for (i in 0 until strokeList.size - 1) {
                        drawLine(
                            color = color,
                            start = strokeList[i],
                            end = strokeList[i + 1],
                            strokeWidth = size,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            blendMode = if (color == Color.Transparent) BlendMode.Clear else BlendMode.SrcOver
                        )
                    }
                }
            }

            BrushShape.CIRCLE -> {
                strokeList.forEach { point ->
                    drawCircle(
                        color = color,
                        radius = size / 2,
                        center = point,
                        blendMode = if (color == Color.Transparent) BlendMode.Clear else BlendMode.SrcOver
                    )
                }
            }

            BrushShape.SQUARE -> {
                strokeList.forEach { point ->
                    drawRect(
                        color = color,
                        topLeft = Offset(point.x - size / 2, point.y - size / 2),
                        size = Size(size, size),
                        blendMode = if (color == Color.Transparent) BlendMode.Clear else BlendMode.SrcOver
                    )
                }
            }
        }
    }
}