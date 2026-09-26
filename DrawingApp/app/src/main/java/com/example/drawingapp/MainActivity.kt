package com.example.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.drawingapp.ui.theme.DrawingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {

            }
        }
    }
}

enum class BrushShape{
    LINE, CIRCLE, SQUARE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen() {
    var brushShape : BrushShape

    Scaffold(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Canvas") },
                navigationIcon = {
                    //TODO: put menu here
                }
            )
        }
    )
    {
        innerPadding ->
        
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .pointerInput(Unit)
                {
                    detectDragGestures(
                        onDragStart = {},
                        onDrag = { change, _ -> },
                        onDragEnd = {}
                    )
                }
        )
        {

        }
    }
}