package com.example.drawingapp.canvas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drawingapp.objdetection.VisionUiState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator

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
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.detectedObjects) { obj ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(obj.label.titleFormat())
                                        Text("${(obj.confidenceScore * 100).toInt()}%")
                                    }
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
 * Helper method used to format the name of the AI detected item in the popup
 */
private fun String.titleFormat():String =
    split(" ").joinToString { word ->
        word.replaceFirstChar { it.uppercase() }
    }