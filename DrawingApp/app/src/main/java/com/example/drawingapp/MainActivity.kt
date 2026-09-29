package com.example.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.drawingapp.ui.theme.DrawingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                CanvasScreen()
            }
        }
    }
}

enum class BrushShape{
    LINE, CIRCLE, SQUARE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen() {
    var brushShape by remember { mutableStateOf(BrushShape.LINE) }
    var brushSize by remember { mutableFloatStateOf(16f) }
    var strokes by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var currentColor by remember { mutableStateOf(Color.Red) }

    Scaffold(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Canvas") },
                navigationIcon = {
                    //TODO: put menu here
                }
            )
        }
    )
    {
        innerPadding ->
        
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .pointerInput(Unit)
                {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke = listOf(offset)
                            strokes = strokes + listOf(currentStroke)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentStroke = currentStroke + change.position
                            strokes = strokes.dropLast(1) + listOf(currentStroke)
                        },
                        onDragEnd = { currentStroke = emptyList() }
                    )
                }
        )
        {
            strokes.forEach { stroke ->
                when (brushShape) {
                    BrushShape.LINE -> {
                        for (i in 0 until stroke.size - 1) {
                            drawLine(currentColor, stroke[i], stroke[i + 1], brushSize)
                        }
                    }
                    BrushShape.CIRCLE -> {
                        stroke.forEach{ point ->
                            drawCircle(currentColor, brushSize, point)
                        }
                    }
                    BrushShape.SQUARE -> {
                        stroke.forEach { point ->
                            drawRect(
                                currentColor,
                                Offset(point.x - brushSize, point.y - brushSize),
                                Size(brushSize, brushSize)
                            )
                        }
                    }
                }
            }
        }
    }
}