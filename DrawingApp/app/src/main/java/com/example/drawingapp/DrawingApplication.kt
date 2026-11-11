package com.example.drawingapp

import android.app.Application
import androidx.room.Room
import com.example.drawingapp.objdetection.ObjectDetectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingApplication : Application() {
    val scope = CoroutineScope(SupervisorJob())

    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DrawingDatabase::class.java,
            "drawing_database"
        ).build()
    }

    val drawingRepository by lazy { DrawingRepository(scope, db.drawingDao()) }
    val visionRepository by lazy { ObjectDetectionRepository() }
}
