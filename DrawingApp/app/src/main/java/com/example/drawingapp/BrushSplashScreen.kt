package com.example.drawingapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

// region Configuration variables
private val StrokeColor = Color(0xFF6200EE)
private val StrokeWidth = 24.dp
private val BrushSize = 192.dp

private const val DRAW_MS = 4500
private const val TEXT_IN_MS = 1500
private const val TEXT_DELAY_MS = 500
private const val FADE_MS = 400
// endregion

/**
 * Brush animation splash screen that is launched when the app is first opened
 * @param onFinished Called once the overlay is completely faded out/done
 */
@Preview
@Composable
fun BrushSplashScreen(onFinished: () -> Unit = {}) {
    var visible by remember { mutableStateOf(true) }
    val progress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    val brush = painterResource(R.drawable.splash_brush)

    LaunchedEffect(Unit) {
        launch { textAlpha.animateTo(1f, tween(TEXT_IN_MS, delayMillis = TEXT_DELAY_MS))}
        progress.animateTo(1f, tween(DRAW_MS))
        launch { textAlpha.animateTo(0f, tween(FADE_MS))}
        overlayAlpha.animateTo(0f, tween(FADE_MS))
        visible = false
        onFinished()
    }

    if (!visible) return;

    Box(Modifier
        .fillMaxSize()
        .zIndex(10f)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {}
    ) {
        Canvas(Modifier
            .fillMaxSize()
            .alpha(overlayAlpha.value)
            .background(Color.White)
        ) {
            drawBrushAnimation(buildWavePath(size), progress.value, brush)
        }

        Text(
            text = "Drawing App",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(textAlpha.value)
        )
    }
}

/**
 * The path of quadratic curves that bend up and down in waves to create
 * animation path
 * @param size Size of the canvas to scale to screen
 * @param waves Number of full waves to draw
 * @param amplitude Wave height as a fraction of canvas height
 */
private fun buildWavePath(size: Size, waves: Int = 4, amplitude: Float = 0.08f): Path {
    val startX = 0f
    val startY = size.height * 0.5f
    val endX = size.width
    val endY = 0f
    val steps = waves * 2
    val amp = size.height * amplitude

    return Path().apply {
        moveTo(startX, startY)
        for (i in 0 until steps) {
            val bend = if (i % 2 == 0) -amp else amp
            quadraticTo(
                startX + (endX - startX) * (i + 0.5f) / steps,
                startY + (endY - startY) * (i + 0.5f) / steps + bend,
                startX + (endX - startX) * (i + 1f) / steps,
                startY + (endY - startY) * (i + 1f) / steps
            )
        }
    }
}

/**
 * Helper method that animates the paint brush drawing animation in the splash screen
 * @param path The path that the paint brush will take
 * @param progress The amount of path to draw from start (0f) to finish (1f)
 * @param brush The icon brush
 */
private fun DrawScope.drawBrushAnimation(path: Path, progress: Float, brush: Painter) {
    val measure = PathMeasure().apply { setPath(path, false) }
    val distance = measure.length * progress

    val trail = Path()
    measure.getSegment(0f, distance, trail, true)
    drawPath(
        trail, StrokeColor,
        style = Stroke(StrokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    val tip = measure.getPosition(distance)
    val b = BrushSize.toPx()
    translate(tip.x - b / 2, tip.y - b / 2) {
        with(brush) { draw(Size(b, b)) }
    }
}