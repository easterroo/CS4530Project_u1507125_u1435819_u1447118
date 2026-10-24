package com.example.drawingapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DrawingRepository(val scope: CoroutineScope, private val dao: DrawingDAO) {
    val allDrawings = scope.launch{ dao.allDrawings() }

    fun saveDrawing(title: String, width: Float, height: Float, drawing: DrawingCanvas) {
        scope.launch {
            val fetchedDrawing = DrawingData(title = title, width = width, height = height, drawing = drawing)
            dao.saveDrawing(fetchedDrawing)
        }
    }

    fun removeDrawing(id: Int) {
        scope.launch { dao.removeDrawing(id) }
    }

    fun getTitle(id: Int): String {
        var title = ""
        scope.launch { title = dao.getTitle(id) }
        return title
    }

    fun updateTitle(title: String, id: Int) {
        scope.launch { dao.updateTitle(title, id) }
    }

    fun getWidth(id: Int): Float {
        var width = 0f
        scope.launch { width = dao.getWidth(id) }
        return width
    }

    fun updateWidth(width: Float, id: Int) {
        scope.launch { dao.updateWidth(width, id) }
    }

    fun getHeight(id: Int): Float {
        var height = 0f
        scope.launch { height = dao.getWidth(id) }
        return height
    }

    fun updateHeight(height: Float, id: Int) {
        scope.launch { dao.updateHeight(height, id) }
    }

    fun getDrawing(id: Int) {
        scope.launch { dao.getDrawing(id) }
    }
}