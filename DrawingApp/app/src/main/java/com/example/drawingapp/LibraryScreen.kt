package com.example.drawingapp

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.core.net.toUri

/**
 * ViewModel representing the drawings that are displayed in the library screen
 */
class LibraryViewModel(private val repository: DrawingRepository) : ViewModel()
{
    private val drawings = MutableStateFlow(listOf<DrawingCanvas>())
    val drawingsSnapshots : StateFlow<List<DrawingCanvas>> = drawings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allDrawings.collect { dataList ->
                drawings.value = dataList.map { data ->
                    DrawingCanvas(
                        id = data.id,
                        title = data.title,
                        width = data.width,
                        height = data.height,
                        filePath = data.filePath
                    )
                }
            }
        }
    }

    /** Returns drawing from selected ID */
    fun getDrawing(id: Int): DrawingCanvas? {
        return drawings.value.find {it.id == id}
    }

    /** Saves drawing as a new one if ID doesn't exist or updates an existing one */
    fun saveDrawing(drawing: DrawingCanvas, context: Context?): DrawingCanvas {
        var finalUriString = drawing.filePath
        var saved = drawing

        if (context != null) {
            val width = if (drawing.width > 0) drawing.width.toInt() else 1000
            val height = if (drawing.height > 0) drawing.height.toInt() else 1000

            val imageBitmap = ImageBitmap(width, height)
            val canvas = androidx.compose.ui.graphics.Canvas(imageBitmap)
            val drawScope = CanvasDrawScope()
            drawScope.draw(
                density = Density(1f),
                layoutDirection = LayoutDirection.Ltr,
                canvas = canvas,
                size = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())
            ) {
                drawRect(color = Color.White)
                drawStrokes(drawing.strokes)
            }

            val bitmap = imageBitmap.asAndroidBitmap()

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME,
                    "${drawing.title}_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                finalUriString = it.toString()
            }
        }

        viewModelScope.launch {
            val dataToSave = DrawingData(
                id = drawing.id,
                title = drawing.title,
                width = drawing.width,
                height = drawing.height,
                filePath = finalUriString
            )

            val generatedId = repository.saveDrawing(dataToSave)
            saved = drawing.copy(id = generatedId.toInt(), filePath = finalUriString)
        }

        return saved
    }

    /** Deleted drawing */
    fun deleteDrawing(drawing: DrawingCanvas, context: Context?) {
        if (context != null && drawing.filePath.isNotEmpty()) {
            val resolver = context.contentResolver
            try {
                val uri = drawing.filePath.toUri()
                resolver.delete(uri, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            repository.removeDrawing(drawing.id)
        }
    }
}

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
                  onNewDrawing: () -> Unit = {}) {
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
                            TextButton(onClick = onNewDrawing) { Text("New DrawingCanvas") }
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
                    onDelete = { myVM.deleteDrawing(drawing, context) }
                )
            }
        }
    }
}

/**
 * Helper method that creates a drawing card that is duplicated
 * on the library screen
 *
 * @param drawing DrawingCanvas object to display on card
 * @param onOpen When card is clicked, opens it up on the canvas
 * @param onDelete When "Delete" is selected from the menu, it is deleted
 */
@Composable
private fun DrawingCard(
    drawing: DrawingCanvas,
    onOpen: () -> Unit,
    onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    Column {
        // Card with the selectable thumbnail and menu option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(onClick = onOpen)
        ) {
            Box(Modifier.fillMaxSize()) {
                DrawingThumbnail(drawing)
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
        // Text showing the drawing's title
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

/**
 * Helper method to create a thumbnail of the drawing to show in the library screen
 *
 * @param drawing The DrawingCanvas object to create a thumbnail from
 */
@Composable
fun DrawingThumbnail(drawing: DrawingCanvas) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen) // so the eraser works
    ) {
        // Scale the saved canvas to fit the card, centered
        val s = minOf(size.width / drawing.width, size.height / drawing.height)
        val dx = (size.width - drawing.width * s) / 2f
        val dy = (size.height - drawing.height * s) / 2f

        withTransform({
            translate(dx, dy)
            scale(s, s, pivot = Offset.Zero)
            clipRect(0f, 0f, drawing.width, drawing.height)
        }) {
            drawStrokes(drawing.strokes)
        }
    }
}