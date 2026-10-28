package com.example.drawingapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Defines the app's navigation and routing between pages
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    canvasViewModel : CanvasViewModel,
    libraryViewModel : LibraryViewModel
) {
    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            LibraryScreen(
                myVM = libraryViewModel,
                onOpenDrawing = { id -> navController.navigate("draw/$id") },
                onNewDrawing = { navController.navigate("draw/new") }
            )
        }
        composable("draw/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            val context = LocalContext.current
            LaunchedEffect(id) {
                if (id != null) {
                    val data = libraryViewModel.getDrawing(id)
                    val bitmap = libraryViewModel.drawingsSnapshots.value[id]
                    canvasViewModel.openDrawing(bitmap, id, data.title, data.width, data.height)
                } else {
                    navController.navigate("draw/new")
                }
            }
            CanvasScreen(
                canvasViewModel = canvasViewModel,
                onSave = { drawing ->
                    val data = DrawingData(
                        id = canvasViewModel.id.value,
                        title = canvasViewModel.title.value
                    )
                    libraryViewModel.saveDrawing(
                        data = data,
                        drawing = drawing,
                        backgroundImage = canvasViewModel.image.value,
                        context = context
                    )
                    navController.popBackStack() // Returns from Canvas back to Library
                }
            )
        }
        composable("draw/new") {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                canvasViewModel.newDrawing()
            }
            CanvasScreen(
                canvasViewModel = canvasViewModel,
                onSave = { drawing ->
                    val data = DrawingData(
                        id = canvasViewModel.id.value,
                        title = canvasViewModel.title.value
                    )
                    libraryViewModel.saveDrawing(
                        data = data,
                        drawing = drawing,
                        backgroundImage = null,
                        context = context
                    )
                    navController.popBackStack() // Returns from Canvas back to Library
                }
            )
        }
    }
}