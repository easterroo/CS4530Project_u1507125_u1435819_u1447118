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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

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
    val drawings = remember { List(6) {Drawing(it, "Drawing")} }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                // TODO: Change this nav bar/buttons and add more later
                actions = {
                    TextButton(onClick = onNewDrawing) { Text("New Drawing")}
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
            items(drawings) { drawing ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onOpenDrawing(drawing.id) }
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(drawing.title)
                    }
                }
            }
        }
    }
}