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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.ui.theme.DrawingAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                AppNavigation()
                BrushSplashScreen()
            }
        }
    }
}

enum class BrushShape{
    LINE, CIRCLE, SQUARE
}

class CanvasViewModel : ViewModel() {
    private val _strokes = MutableStateFlow(listOf<List<Offset>>())
    private val _brushSize = MutableStateFlow(16f)
    private val _brushShape = MutableStateFlow(BrushShape.LINE)
    private val _currentColor = MutableStateFlow(Color.Red)

    val strokes : StateFlow<List<List<Offset>>> = _strokes
    val brushSize : StateFlow<Float> = _brushSize
    val brushShape : StateFlow<BrushShape> = _brushShape
    val currentColor : StateFlow<Color> = _currentColor

    fun addStroke(currentStroke: List<Offset>) {
        _strokes.value += listOf(currentStroke)
    }

    fun setBrushSize(newBrushSize: Float) {
        _brushSize.value = newBrushSize
    }

    fun setBrushShape(newBrushShape: BrushShape) {
        _brushShape.value = newBrushShape
    }

    fun setColor(newColor: Color) {
        _currentColor.value = newColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(canvasViewModel: CanvasViewModel) {
    val brushShape by canvasViewModel.brushShape.collectAsStateWithLifecycle()
    val brushSize by canvasViewModel.brushSize.collectAsStateWithLifecycle()
    val currentColor by canvasViewModel.currentColor.collectAsStateWithLifecycle()
    val strokes by canvasViewModel.strokes.collectAsStateWithLifecycle()
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

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
                        onDragStart = { offset ->
                            currentStroke = listOf(offset)
                            canvasViewModel.addStroke(currentStroke)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            change.historical.forEach { historical ->
                                currentStroke = currentStroke + historical.position
                            }
                            currentStroke = currentStroke + change.position
                            canvasViewModel.addStroke(currentStroke)
                        },
                        onDragEnd = { currentStroke = emptyList() }
                    )
                }
        )
        {
            strokes.forEach { stroke ->
                when (brushShape) {
                    BrushShape.LINE -> {
                        for (i in 0 until stroke.size - 1) {
                            drawLine(currentColor, stroke[i], stroke[i + 1], brushSize)
                        }
                    }
                    BrushShape.CIRCLE -> {
                        stroke.forEach{ point ->
                            drawCircle(currentColor, brushSize, point)
                        }
                    }
                    BrushShape.SQUARE -> {
                        stroke.forEach { point ->
                            drawRect(
                                currentColor,
                                Offset(point.x - brushSize, point.y - brushSize),
                                Size(brushSize, brushSize)
                            )
                        }
                    }
                }
            }
        }
    }
}