package com.example.drawingapp

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
import androidx.compose.ui.text.style.TextOverflow

data class Drawing (
    val id: Int,
    val title: String
    // TODO: Class that represents drawing
)

class LibraryViewModel : ViewModel()
{
    private val drawings = MutableStateFlow(listOf<Drawing>())
    // TODO: Add functions and other params for the view model
}

/**
 * Main library screen that allows you to add, edit, and delete drawings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpenDrawing: (Int) -> Unit = {},
                  onNewDrawing: () -> Unit = {}) {
    val drawings = remember { List(6) {Drawing(it, "Drawing")}.toMutableStateList() }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.LightGray
                ),
                title = {
                    Column {
                        Text("My Drawings")
                        // TODO: Change this nav bar/buttons and add more later
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
                    onDelete = { drawings.remove(drawing) }
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
                // TODO: Thumbnail of drawing
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