package com.example.drawingapp

import android.app.Application
import androidx.room.Room
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

}
