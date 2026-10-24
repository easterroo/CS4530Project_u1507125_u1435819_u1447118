package com.example.drawingapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DrawingDAO {
    @Insert
    suspend fun saveDrawing(drawing: DrawingData)

    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun removeDrawing(id: Int)

    @Query("SELECT title from drawings WHERE id = :id")
    suspend fun getTitle(id: Int): String

    @Query("UPDATE drawings SET title = :title WHERE id = :id")
    suspend fun updateTitle(title: String, id: Int)

    @Query("SELECT width FROM drawings WHERE id = :id")
    suspend fun getWidth(id: Int): Float

    @Query("UPDATE drawings SET width = :width WHERE id = :id")
    suspend fun updateWidth(width: Float, id: Int)

    @Query("SELECT height FROM drawings WHERE id = :id")
    suspend fun getHeight(id: Int): Float

    @Query("UPDATE drawings SET height = :height WHERE id = :id")
    suspend fun updateHeight(height: Float, id: Int)

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawing(id: Int): DrawingData?

    @Query("SELECT * FROM drawings ORDER BY id")
    suspend fun allDrawings()
}