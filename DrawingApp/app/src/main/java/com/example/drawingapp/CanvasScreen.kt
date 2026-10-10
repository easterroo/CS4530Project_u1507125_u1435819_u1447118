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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Square
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
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
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
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
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                }
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        BrushShapeSelectionButton(BrushShape.LINE, canvasViewModel::setBrushShape)
                        BrushShapeSelectionButton(BrushShape.CIRCLE, canvasViewModel::setBrushShape)
                        BrushShapeSelectionButton(BrushShape.SQUARE, canvasViewModel::setBrushShape)

                        var expanded by remember { mutableStateOf(false) }
                        val presetSizes = listOf("4", "8", "12", "16", "20", "24", "28", "32", "36", "40", "44", "48")
                        var brushSizeInput by remember { mutableStateOf(brushSize.toInt().toString()) }

                        Box(modifier = Modifier
                            .width(105.dp)
                            .border(width = 2.dp, color = LocalContentColor.current)
                        )
                        {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .testTag("titleField"),
                                value = brushSizeInput,
                                onValueChange = { newSize ->
                                    brushSizeInput = newSize
                                    if (newSize.all { it.isDigit() } && newSize.isNotEmpty() && newSize.length <= 2) {
                                        canvasViewModel.setBrushSize(newSize.toFloat())
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                presetSizes.forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text(size) },
                                        onClick = {
                                            brushSizeInput = size
                                            expanded = false
                                            canvasViewModel.setBrushSize(size.toFloat())
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }
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
                    .onSizeChanged {
                        canvasViewModel.setCanvasSize(
                            it.width.toFloat(),
                            it.height.toFloat()
                        )
                    }
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .pointerInput(Unit)
                    {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val baseWidth = if (drawing.canvasWidth > 1f) drawing.canvasWidth else size.width.toFloat().coerceAtLeast(1f)
                                val baseHeight = if (drawing.canvasHeight > 1f) drawing.canvasHeight else size.height.toFloat().coerceAtLeast(1f)
                                val s = minOf(size.width.toFloat() / baseWidth, size.height.toFloat() / baseHeight)
                                val dx = (size.width.toFloat() - baseWidth * s) / 2f
                                val dy = (size.height.toFloat() - baseHeight * s) / 2f

                                val logicalOffset = Offset(
                                    x = (offset.x - dx) / s,
                                    y = (offset.y - dy) / s
                                )

                                currentStrokeList = listOf(logicalOffset)

                                val newStroke = Stroke(
                                    stroke = currentStrokeList,
                                    shape = brushShape,
                                    size = brushSize,
                                    color = if (eraseMode) Color.Transparent else currentColor
                                )

                                canvasViewModel.addStroke(newStroke)
                            },
                            onDrag = { change, _ ->
                                val baseWidth = if (drawing.canvasWidth > 1f) drawing.canvasWidth else size.width.toFloat().coerceAtLeast(1f)
                                val baseHeight = if (drawing.canvasHeight > 1f) drawing.canvasHeight else size.height.toFloat().coerceAtLeast(1f)
                                val s = minOf(size.width.toFloat() / baseWidth, size.height.toFloat() / baseHeight)
                                val dx = (size.width.toFloat() - baseWidth * s) / 2f
                                val dy = (size.height.toFloat() - baseHeight * s) / 2f

                                val historicalPoints = change.historical.map { 
                                    Offset(
                                        x = (it.position.x - dx) / s,
                                        y = (it.position.y - dy) / s
                                    )
                                }
                                val currentLogical = Offset(
                                    x = (change.position.x - dx) / s,
                                    y = (change.position.y - dy) / s
                                )

                                currentStrokeList =
                                    currentStrokeList + historicalPoints + currentLogical

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
                val baseWidth = if (drawing.canvasWidth > 1f) drawing.canvasWidth else size.width.coerceAtLeast(1f)
                val baseHeight = if (drawing.canvasHeight > 1f) drawing.canvasHeight else size.height.coerceAtLeast(1f)

                val s = minOf(size.width / baseWidth, size.height / baseHeight)
                val dx = (size.width - baseWidth * s) / 2f
                val dy = (size.height - baseHeight * s) / 2f

                withTransform({
                    translate(dx, dy)
                    scale(s, s, pivot = Offset.Zero)
                    clipRect(0f, 0f, baseWidth, baseHeight)
                }) {
                    drawStrokes(strokes)
                }
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
 * @param selectionFunction the function used to select a brush shape
 */
@Composable
fun BrushShapeSelectionButton(brushShape: BrushShape, selectionFunction: (BrushShape) -> Unit) {
    IconButton(
        content = {
            when (brushShape) {
                BrushShape.LINE -> {
                    Icon(
                        painter = painterResource(R.drawable.line),
                        contentDescription = "Eraser"
                    )
                }
                BrushShape.CIRCLE -> {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = "Circle"
                    )
                }
                BrushShape.SQUARE -> {
                    Icon(
                        imageVector = Icons.Outlined.Square,
                        contentDescription = "Square"
                    )
                }
            }
        },
        onClick = { selectionFunction(brushShape) }
    )
}