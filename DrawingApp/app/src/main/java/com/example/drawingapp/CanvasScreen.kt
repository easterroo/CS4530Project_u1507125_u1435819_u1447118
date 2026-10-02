package com.example.drawingapp

import android.graphics.drawable.Icon
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(canvasViewModel: CanvasViewModel) {
    val drawing by canvasViewModel.drawing.collectAsStateWithLifecycle()
    val brushShape by canvasViewModel.brushShape.collectAsStateWithLifecycle()
    val brushSize by canvasViewModel.brushSize.collectAsStateWithLifecycle()
    val currentColor by canvasViewModel.currentColor.collectAsStateWithLifecycle()
    val strokes = drawing.strokes
    
    var eraseMode by remember { mutableStateOf(false) }
    var currentStrokeList by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var isEditingTitle by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { 
                    if (isEditingTitle) {
                        TextField(
                            value = drawing.title,
                            onValueChange = { canvasViewModel.updateTitle(it) },
                            textStyle = TextStyle(fontSize = 22.sp),
                            singleLine = true
                        )

                    } else {
                        Text(
                            text = drawing.title,
                            modifier = Modifier.clickable { isEditingTitle = true }
                        )
                    }
                },
                actions = {
                    if (isEditingTitle) {
                        TextButton(
                            onClick = { 
                                isEditingTitle = false
                            }
                        ) {
                            Text("Done")
                        }

                    } else {
                        IconButton(
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = "Save"
                                )
                                      },
                            onClick = {
                                canvasViewModel.saveCanvas()
                            }
                        )
                    }
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

                                val newStroke = Stroke(
                                    stroke = currentStrokeList,
                                    shape = brushShape,
                                    size = brushSize,
                                    color = if (eraseMode) Color.Transparent else currentColor
                                )

                                canvasViewModel.addStroke(newStroke)
                            },
                            onDrag = { change, _ ->
                                val historicalPoints = change.historical.map { it.position }
                                currentStrokeList = currentStrokeList + historicalPoints + change.position

                                val updatedStroke = Stroke(
                                    stroke = currentStrokeList,
                                    shape = brushShape,
                                    size = brushSize,
                                    color = if (eraseMode) Color.Transparent else currentColor
                                )

                                canvasViewModel.updateLastStroke(updatedStroke)
                            },
                            onDragEnd = { currentStrokeList = emptyList() }
                        )
                    }
            )
            {
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