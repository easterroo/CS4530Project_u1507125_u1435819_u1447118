package com.example.drawingapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Defines the app's navigation and routing between pages
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val canvasViewModel : CanvasViewModel = viewModel()
    val libraryViewModel : LibraryViewModel = viewModel()

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            LibraryScreen(
                myVM = libraryViewModel,
                onOpenDrawing = { id -> navController.navigate("draw/$id") },
                onNewDrawing = { navController.navigate("draw/new") }
            )
        }
        composable("draw/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")

            LaunchedEffect(id) {
                canvasViewModel.openDrawing(id?.toIntOrNull()?.let {libraryViewModel.getDrawing(it) })
            }
            CanvasScreen(
                canvasViewModel = canvasViewModel,
                onSave = {drawing ->
                    val saved = libraryViewModel.saveDrawing(drawing)
                    canvasViewModel.setId(saved.id)
                    navController.popBackStack() // Returns from Canvas back to Library
                }
            )
        }
    }
}