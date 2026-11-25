package com.example.drawingapp

import android.app.Application
import androidx.room.Room
import com.example.drawingapp.auth.AuthRepository
import com.example.drawingapp.cloud.CloudDrawingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.data.DrawingDatabase

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
    val authRepository by lazy { AuthRepository() }
    val cloudDrawingRepository by lazy { CloudDrawingRepository() }
}
