package com.example.drawingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class DrawingData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "Unnamed Drawing",
    //TODO: determine if width and height should be in here or in DrawingCanvas
    val width: Float = 1f,
    val height: Float = 1f,
    //TODO: remove duplicate fields from DrawingCanvas, make TypeConverter to serialize
    val drawing: DrawingCanvas
)
