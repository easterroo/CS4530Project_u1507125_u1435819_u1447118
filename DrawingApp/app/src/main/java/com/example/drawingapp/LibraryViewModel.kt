package com.example.drawingapp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            withContext(Dispatchers.IO) {
                val drawing = repository.getDrawing(id)
                if (drawing != null && drawing.filePath.isNotBlank()) {
                    try {
                        File(drawing.filePath).delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            repository.removeDrawing(id)
        }
    }

    fun shareDrawing(id: Int, context: Context) {
        viewModelScope.launch {
            val fetchedDrawing = repository.getDrawing(id) ?: return@launch
            val file = File(fetchedDrawing.filePath)
            if (!file.exists()) return@launch
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share drawing"))
        }
    }

    fun importImage(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
            // 1. Read just the dimensions
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@launch

            // 2. Decode downsampled to avoid out-of-memory on big photos
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1000) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val source = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return@launch

            // 3. Scale so the longest side is 1000
            val scale = 1000f / maxOf(source.width, source.height)
            val w = (source.width * scale).toInt().coerceAtLeast(1)
            val h = (source.height * scale).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(source, w, h, true)
            // 4. Flatten onto white (handles transparent PNGs) and save
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            android.graphics.Canvas(out).apply {
                drawColor(android.graphics.Color.WHITE)
                drawBitmap(scaled, 0f, 0f, null)
            }
            val dir = File(context.filesDir, "drawings").apply { mkdirs() }
            val file = File(dir, "drawing_${System.currentTimeMillis()}.png")
            file.outputStream().use { out.compress(Bitmap.CompressFormat.PNG, 100, it) }

            repository.addDrawing(
                DrawingData(
                    title = "Imported Image",
                    width = w.toFloat(),
                    height = h.toFloat(),
                    filePath = file.absolutePath
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        }
    }
}