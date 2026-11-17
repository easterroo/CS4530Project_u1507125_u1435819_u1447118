package com.example.drawingapp.canvas


import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawingapp.objdetection.DetectedObject
import com.example.drawingapp.objdetection.VisionUiState

/** Random list of colors to use to highlight bounding boxes when AI detecting */
private val boxColors = listOf(
    Color.Red, Color.Blue, Color(0xFF2E7D32), Color.Magenta, Color(0xFFFF8000)
)

/**
 * Popup when using AI analysis tool in canvas
 */
@Composable
fun AnalysisPopup(
    state: VisionUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Analysis") },
        text = {
            when (state) {
                VisionUiState.Idle, VisionUiState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Analyzing your drawing...")
                    }
                }
                is VisionUiState.Error -> Text("Error: ${state.message}")
                is VisionUiState.Success -> {
                    if (state.detectedObjects.isEmpty()) {
                        Text("No objects were detected.")
                    } else {
                        val bitmap = remember(state.imageFile) {
                            BitmapFactory.decodeFile(state.imageFile.absolutePath)?.asImageBitmap()
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (bitmap != null) {
                                item { DetectionOverlay(bitmap, state.detectedObjects) }
                            }
                            itemsIndexed(state.detectedObjects) { i, obj ->
                                val b = obj.boundingBox
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(obj.label.titleFormat())
                                        Text("${(obj.confidenceScore * 100).toInt()}%")
                                    }
                                    Text (
                                        "Box: (${b.minX}, ${b.minY}) to (${b.maxX}, ${b.maxY})",
                                        style = MaterialTheme.typography.bodySmall
                                        )
                                    LinearProgressIndicator(
                                        progress = { obj.confidenceScore.coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
            }) { Text("OK") }
        }
    )
}

/**
 * Draws the analyzed image with a labeled box around each detected object
 */
@Composable
private fun DetectionOverlay(image: ImageBitmap, objects: List<DetectedObject>) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(image.width.toFloat() / image.height)
    ) {
        drawImage(image, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
        objects.forEachIndexed { i, obj ->
            val b = obj.boundingBox
            val color = boxColors[i % boxColors.size]
            val left = b.minX / 1000f * size.width
            val top = b.minY / 1000f * size.height
            val right = b.maxX / 1000f * size.width
            val bottom = b.maxY / 1000f * size.height

            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = DrawStroke(width = 4f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = obj.label.titleFormat(),
                topLeft = Offset(left, top),
                style = TextStyle(color = Color.White, fontSize = 12.sp, background = color)
            )
        }
    }
}

/**
 * Helper method used to format the name of the AI detected item in the popup
 */
private fun String.titleFormat():String =
    split(" ").joinToString { word ->
        word.replaceFirstChar { it.uppercase() }
    }