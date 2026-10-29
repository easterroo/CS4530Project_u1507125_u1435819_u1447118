package com.example.drawingapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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
                onNewDrawing = { navController.navigate("draw") }
            )
        }
        composable("draw/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val context = LocalContext.current
            var initialized by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(id) {
                if (!initialized) {
                    initialized = true
                    val data = libraryViewModel.getDrawing(id)
                    val bitmap = libraryViewModel.drawingsSnapshots.value[id]
                    canvasViewModel.openDrawing(bitmap, id, data.title, data.width, data.height)
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
        composable("draw") {
            val context = LocalContext.current
            var initialized by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (!initialized) {
                    initialized = true
                    canvasViewModel.newDrawing()
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
                        backgroundImage = null,
                        context = context
                    )
                    navController.popBackStack() // Returns from Canvas back to Library
                }
            )
        }
    }
}