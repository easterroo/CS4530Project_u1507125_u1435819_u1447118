package com.example.drawingapp

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel representing the drawings that are displayed in the library screen
 */
class LibraryViewModel(
    private val repository: DrawingRepository,
    private val application: Application? = null
) : ViewModel()
{
    private val _drawings = MutableStateFlow<Map<Int, ImageBitmap>>(emptyMap())
    val drawingsSnapshots : StateFlow<Map<Int, ImageBitmap>> = _drawings.asStateFlow()

    private val _drawingsData = MutableStateFlow<Map<Int, DrawingData>>(emptyMap())

    init {
        viewModelScope.launch {
            repository.allDrawings.collect { dataList ->
                val imagesMap = mutableMapOf<Int, ImageBitmap>()
                val dataMap = mutableMapOf<Int, DrawingData>()
                dataList.forEach { data ->
                    dataMap[data.id] = data
                    val loaded = getImage(data.filePath, application)
                    if (loaded != null) {
                        imagesMap[data.id] = loaded
                    }
                }
                _drawingsData.value = dataMap
                _drawings.value = imagesMap
            }
        }
    }

    fun getDrawing(id: Int): DrawingData {
        return _drawingsData.value[id] ?: DrawingData(id = id)
    }

    private fun getImage(filePath: String, context: Context? = null): ImageBitmap? {
        if (filePath.isBlank()) return null
        try {
            val file = File(filePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    return bitmap.asImageBitmap()
                }
            }
            val ctx = context ?: application
            if (ctx != null) {
                val uri = filePath.toUri()
                ctx.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        return bitmap.asImageBitmap()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /** Saves drawing as a new one if ID doesn't exist or updates an existing one */
    fun saveDrawing(
        data: DrawingData,
        drawing: DrawingCanvas,
        backgroundImage: ImageBitmap? = null,
        context: Context?
    ) {
        viewModelScope.launch {
            val existing = if (data.id != 0) repository.getDrawing(data.id) else null

            val existingImage = if (backgroundImage != null && backgroundImage.width > 1 && backgroundImage.height > 1) {
                backgroundImage
            } else if (existing != null && existing.filePath.isNotBlank()) {
                getImage(existing.filePath, context ?: application)
            } else {
                null
            }

            var finalUriString = ""

            if (context != null) {
                val width = when {
                    drawing.width > 1f -> drawing.width.toInt()
                    else -> 1000
                }
                val height = when {
                    drawing.height > 1f -> drawing.height.toInt()
                    else -> 1000
                }

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
                    if (existingImage != null) {
                        drawImage(
                            image = existingImage,
                            dstSize = androidx.compose.ui.unit.IntSize(width, height)
                        )
                    }
                    drawStrokes(drawing.strokes)
                }

                val bitmap = imageBitmap.asAndroidBitmap()

                val imagesDir = File(context.filesDir, "drawings").apply { mkdirs() }
                val imageFile = File(imagesDir, "drawing_${System.currentTimeMillis()}.png")
                try {
                    imageFile.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    finalUriString = imageFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (existing != null) {
                if (finalUriString.isNotEmpty()) {
                    if (existing.filePath.isNotEmpty() && existing.filePath != finalUriString) {
                        try {
                            File(existing.filePath).delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    repository.updateFilePath(finalUriString, data.id)
                }
                repository.updateTitle(data.title, data.id)
                repository.updateWidth(drawing.width, data.id)
                repository.updateHeight(drawing.height, data.id)
            } else {
                val newDrawingData = DrawingData(
                    title = if (data.title.isNotBlank()) data.title else "Untitled Drawing",
                    width = drawing.width,
                    height = drawing.height,
                    filePath = finalUriString
                )
                repository.addDrawing(newDrawingData)
            }
        }
    }

    /** Deleted drawing */
    fun deleteDrawing(id: Int, context: Context?) {
        viewModelScope.launch {
            val fetchedDrawing = repository.getDrawing(id)
            if (context != null && fetchedDrawing != null && fetchedDrawing.filePath.isNotEmpty()) {
                val resolver = context.contentResolver
                try {
                    val uri = fetchedDrawing.filePath.toUri()
                    resolver.delete(uri, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.removeDrawing(id)
        }
    }
}