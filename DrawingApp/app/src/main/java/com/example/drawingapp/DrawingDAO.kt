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

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawing(id: Int): DrawingData?

    @Query("SELECT * FROM drawings ORDER BY id")
    suspend fun allDrawings()
}