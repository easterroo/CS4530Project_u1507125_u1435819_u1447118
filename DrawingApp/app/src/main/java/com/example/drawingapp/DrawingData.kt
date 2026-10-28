package com.example.drawingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class DrawingData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "Untitled Drawing",
    val width: Float = 1f,
    val height: Float = 1f,
    val filePath: String = ""
)
