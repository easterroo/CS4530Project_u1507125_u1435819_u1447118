package com.example.drawingapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DrawingRepository(val scope: CoroutineScope, private val dao: DrawingDAO) {
    val allDrawings = dao.allDrawings()

    suspend fun saveDrawing(drawing: DrawingData): Long {
        return dao.saveDrawing(drawing)
    }

    fun removeDrawing(id: Int) {
        scope.launch { dao.removeDrawing(id) }
    }

    fun updateTitle(title: String, id: Int) {
        scope.launch { dao.updateTitle(title, id) }
    }

    fun updateWidth(width: Float, id: Int) {
        scope.launch { dao.updateWidth(width, id) }
    }

    fun updateHeight(height: Float, id: Int) {
        scope.launch { dao.updateHeight(height, id) }
    }

    fun updateFilePath(filePath: String, id: Int) {
        scope.launch { dao.updateFilePath(filePath, id) }
    }

    fun getDrawing(id: Int) {
        scope.launch { dao.getDrawing(id) }
    }
}