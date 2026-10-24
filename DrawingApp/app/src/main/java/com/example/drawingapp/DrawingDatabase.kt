package com.example.drawingapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DrawingData::class], version = 1, exportSchema = false)
abstract class DrawingDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDAO
}