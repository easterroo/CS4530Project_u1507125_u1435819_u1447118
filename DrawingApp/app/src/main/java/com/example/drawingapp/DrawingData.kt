package com.example.drawingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class DrawingData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val drawing: Drawing
)
