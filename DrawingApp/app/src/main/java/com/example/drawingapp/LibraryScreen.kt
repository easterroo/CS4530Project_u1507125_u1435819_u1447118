package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext



/**
 * Main library screen that allows you to add, edit, and delete drawings
 *
 * @param myVM ViewModel used to retrieve the drawings to display
 * @param onOpenDrawing Calls the correct navigation path when clicked
 * @param onNewDrawing Calls the correct navigation to new drawing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(myVM: LibraryViewModel = viewModel(),
                  onOpenDrawing: (Int) -> Unit = {},
                  onNewDrawing: () -> Unit = {},
                  onImportImage: () -> Unit = {}) {
    val drawings by myVM.drawingsSnapshots.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                            TextButton(onClick = onImportImage) { Text("Import Image")}
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
            items(drawings.entries.toList(), key = {entry -> entry.key}) { drawing ->
                DrawingCard(
                    thumbnail = drawing.value.copy(),
                    title = myVM.getDrawing(drawing.key).title,
                    onOpen = { onOpenDrawing(drawing.key) },
                    onDelete = { myVM.deleteDrawing(drawing.key, context) },
                    onExport = { myVM.shareDrawing(drawing.key, context)}
                )
            }
        }
    }
}

/**
 * Helper method that creates a drawing card that is duplicated
 * on the library screen
 *
 * @param thumbnail Image to display on card
 * @param title The title of the drawing
 * @param onOpen When card is clicked, opens it up on the canvas
 * @param onDelete When "Delete" is selected from the menu, it is deleted
 */
@Composable
private fun DrawingCard(
    thumbnail: ImageBitmap,
    title: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    Column {
        // Card with the selectable thumbnail and menu option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(onClick = onOpen)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = thumbnail,
                    contentDescription = title,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize()
                )
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
                        DropdownMenuItem(
                            text = { Text("Export") },
                            onClick = {
                                menuOpen = false;
                                onExport()
                            }
                        )
                    }
                }
            }
        }
        // Text showing the drawing's title
        Text(
            text = title,
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

/**
 * Creates a mutable copy of an ImageBitmap so modifications do not alter the cached original.
 */
fun ImageBitmap.copy(): ImageBitmap {
    val androidBitmap = this.asAndroidBitmap()
    val config = androidBitmap.config ?: Bitmap.Config.ARGB_8888
    return androidBitmap.copy(config, true).asImageBitmap()
}