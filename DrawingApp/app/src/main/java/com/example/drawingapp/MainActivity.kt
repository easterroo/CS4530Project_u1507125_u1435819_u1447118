package com.example.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.drawingapp.ui.theme.DrawingAppTheme

/**
 * Main Activity used to run the Drawing App program.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val showSplash = savedInstanceState == null
        setContent {
            DrawingAppTheme {
                val canvasViewModel : CanvasViewModel by viewModels{ DrawingViewModelProvider.Factory }
                val libraryViewModel : LibraryViewModel by viewModels{ DrawingViewModelProvider.Factory }

                AppNavigation(canvasViewModel = canvasViewModel, libraryViewModel = libraryViewModel)
                if (showSplash) BrushSplashScreen()
            }
        }
    }
}