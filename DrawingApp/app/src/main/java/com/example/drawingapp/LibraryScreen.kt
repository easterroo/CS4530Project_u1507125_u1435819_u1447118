package com.example.drawingapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.withTransform

class LibraryViewModel : ViewModel()
{
    private val drawings = MutableStateFlow(listOf<Drawing>())
    val drawingsSnapshots : StateFlow<List<Drawing>> = drawings.asStateFlow()

    private var nextId = 1

    // TODO: Add functions and other params for the view model

    /** Returns drawing from selected ID */
    fun getDrawing(id: Int): Drawing? {
        return drawings.value.find {it.id == id}
    }

    /** Saves drawing as a new one if ID doesn't exist or updates an existing one */
    fun saveDrawing(drawing: Drawing): Drawing {
        val saved = if (drawing.id == 0) drawing.copy(id = nextId++) else drawing

        drawings.update { list->
            if (list.any {it.id == saved.id }) list.map {
                if (it.id == saved.id) saved else it
            }
            else list + saved
        }
        return saved
    }

    /** Deleted drawing */
    fun deleteDrawing(drawing: Drawing) {
        drawings.value -= drawing
    }
}

/**
 * Main library screen that allows you to add, edit, and delete drawings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(myVM: LibraryViewModel = viewModel(), onOpenDrawing: (Int) -> Unit = {},
                  onNewDrawing: () -> Unit = {}) {
    val drawings by myVM.drawingsSnapshots.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.LightGray
                ),
                title = {
                    Column {
                        Text("My Drawings")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = onNewDrawing) { Text("New Drawing") }
                            // TODO: TextButton(onClick = onNewDrawing) { Text("Select")}
                            // TODO: TextButton(onClick = onNewDrawing) { Text("Cloud & Backup")}
                            // TODO: TextButton(onClick = onNewDrawing) { Text("Sharing")}
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(drawings, key = { it.id }) { drawing ->
                DrawingCard(
                    drawing = drawing,
                    onOpen = { onOpenDrawing(drawing.id) },
                    onDelete = { myVM.deleteDrawing(drawing) }
                )
            }
        }
    }
}

/**
 * Helper method that creates a drawing card that is duplicated
 * on the library screen
 *
 * @param drawing Drawing object to display on card
 * @param onOpen When card is clicked, opens it up on the canvas
 * @param onDelete When "Delete" is selected from the menu, it is deleted
 */
@Composable
private fun DrawingCard(
    drawing: Drawing,
    onOpen: () -> Unit,
    onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(onClick = onOpen)
        ) {
            Box(Modifier.fillMaxSize()) {
                DrawingThumbnail(drawing, Modifier.fillMaxSize())
                Box(Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("⋮")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }

        Text(
            text = drawing.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun DrawingThumbnail(drawing: Drawing, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .background(Color.White)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen) // so the eraser works
    ) {
        // Scale the saved canvas to fit the card, centered
        val s = minOf(size.width / drawing.canvasWidth, size.height / drawing.canvasHeight)
        val dx = (size.width - drawing.canvasWidth * s) / 2f
        val dy = (size.height - drawing.canvasHeight * s) / 2f

        withTransform({
            translate(dx, dy)
            scale(s, s, pivot = Offset.Zero)
            clipRect(0f, 0f, drawing.canvasWidth, drawing.canvasHeight)
        }) {
            drawStrokes(drawing.strokes)
        }
    }
}