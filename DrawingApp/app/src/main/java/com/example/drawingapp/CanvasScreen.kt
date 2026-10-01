package com.example.drawingapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(canvasViewModel: CanvasViewModel) {
    val brushShape by canvasViewModel.brushShape.collectAsStateWithLifecycle()
    val brushSize by canvasViewModel.brushSize.collectAsStateWithLifecycle()
    val currentColor by canvasViewModel.currentColor.collectAsStateWithLifecycle()
    val strokes by canvasViewModel.strokes.collectAsStateWithLifecycle()
    var eraseMode by remember { mutableStateOf(false) }
    var currentStroke by remember { mutableStateOf(Stroke()) }
    var currentStrokeList by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Scaffold(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Canvas") },
                navigationIcon = {
                    //TODO: put menu here
                }
            )
        },
        bottomBar = {
            NavigationBar {
                var selectedIndex by rememberSaveable { mutableStateOf(0) }

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Brush,
                            contentDescription = "Brush"
                        )
                    },
                    selected = selectedIndex == 0,
                    onClick = {
                        eraseMode = false
                        selectedIndex = 0
                    }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.eraser),
                            contentDescription = "Eraser"
                        )
                    },
                    selected = selectedIndex == 1,
                    onClick = {
                        eraseMode = true
                        selectedIndex = 1
                    }
                )
            }
        }
    )
    {
            innerPadding ->

        // Prevent background of canvas from being erased
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        )
        {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .pointerInput(Unit)
                    {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStrokeList = listOf(offset)

                                val newStroke = Stroke().apply {
                                    setSize(brushSize)
                                    setShape(brushShape)
                                    setColor(if (eraseMode) Color.Transparent else currentColor)
                                    setStroke(currentStrokeList)
                                }
                                currentStroke = newStroke

                                canvasViewModel.addStroke(newStroke)
                            },
                            onDrag = { change, _ ->
                                val historicalPoints = change.historical.map { it.position }
                                currentStrokeList = currentStrokeList + historicalPoints + change.position

                                val updatedStroke = Stroke().apply {
                                    setSize(brushSize)
                                    setShape(brushShape)
                                    setColor(if (eraseMode) Color.Transparent else currentColor)
                                    setStroke(currentStrokeList)
                                }
                                currentStroke = updatedStroke

                                canvasViewModel.updateLastStroke(updatedStroke)
                            },
                            onDragEnd = { currentStrokeList = emptyList() }
                        )
                    }
            )
            {
                strokes.forEach { stroke ->
                    val color = stroke.strokeColor.value
                    val size = stroke.strokeSize.value
                    val strokeList = stroke.stroke.value

                    when (stroke.strokeShape.value) {
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
                            strokeList.forEach{ point ->
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
        }
    }
}