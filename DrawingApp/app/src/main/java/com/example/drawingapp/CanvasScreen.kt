package com.example.drawingapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Initializes the Canvas Screen that is used to draw on to update drawings
 *
 * @param canvasViewModel ViewModel used to save information that is changed through Canvas
 * @param onSave Calls the correct navigation when save is clicked back to library screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(canvasViewModel: CanvasViewModel, onSave: (Drawing) -> Unit) {
    val drawing by canvasViewModel.drawing.collectAsStateWithLifecycle()
    val brushShape by canvasViewModel.brushShape.collectAsStateWithLifecycle()
    val brushSize by canvasViewModel.brushSize.collectAsStateWithLifecycle()
    val currentColor by canvasViewModel.currentColor.collectAsStateWithLifecycle()
    val strokes = drawing.strokes
    
    var eraseMode by remember { mutableStateOf(false) }
    var currentStrokeList by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var isEditingTitle by remember { mutableStateOf(false) }
    var colorSelectionMenu by remember { mutableStateOf(true) }

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
                                onSave(drawing)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth())
            {
                AnimatedVisibility(
                    visible = colorSelectionMenu,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                )
                {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        ColorSelectionButton(Color.Red, canvasViewModel::setColor)
                        ColorSelectionButton(Color(0xFFFF8000), canvasViewModel::setColor)
                        ColorSelectionButton(Color.Yellow, canvasViewModel::setColor)
                        ColorSelectionButton(Color.Green, canvasViewModel::setColor)
                        ColorSelectionButton(Color.Blue, canvasViewModel::setColor)
                        ColorSelectionButton(Color(0xFF87C3FA), canvasViewModel::setColor)
                        ColorSelectionButton(Color(0xFF800080), canvasViewModel::setColor)
                        ColorSelectionButton(Color(0xFF654321), canvasViewModel::setColor)
                        ColorSelectionButton(Color.Magenta, canvasViewModel::setColor)
                        ColorSelectionButton(Color.Black, canvasViewModel::setColor)
                        ColorSelectionButton(Color.White, canvasViewModel::setColor)
                    }
                }
                BottomAppBar(
                    actions = {
                        IconButton(
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Brush,
                                    contentDescription = "Brush"
                                )
                            },
                            onClick = {
                                eraseMode = false
                                colorSelectionMenu = true
                            }
                        )
                        IconButton(
                            content = {
                                Icon(
                                    painter = painterResource(R.drawable.eraser),
                                    contentDescription = "Eraser"
                                )
                            },
                            onClick = {
                                eraseMode = true
                                colorSelectionMenu = false
                            }
                        )
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
                    .onSizeChanged{ canvasViewModel.setCanvasSize(it.width.toFloat(), it.height.toFloat())}
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
                drawStrokes(strokes)
            }
        }
    }
}

/**
 * Used to create set colors that can be selected to draw with
 *
 * @param color The color to select
 * @param selectionFunction the function used to select a color
 */
@Composable
fun ColorSelectionButton(color: Color, selectionFunction: (Color) -> Unit) {
    Box(modifier = Modifier
        .size(24.dp)
        .border(
            width = 2.dp,
            color = LocalContentColor.current,
            shape = CircleShape
        ),
        contentAlignment = Alignment.Center
    )
    {
        IconButton(
            content = {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    tint = color,
                    contentDescription = "Red"
                )
            },
            onClick = { selectionFunction(color) }
        )
    }
}

/**
 * Used to create set shapes that can be selected to draw with
 *
 * @param brushShape The selected brushShape
 */
@Composable
fun BrushShapeSelectionButton(brushShape: BrushShape, canvasViewModel: CanvasViewModel) {

}