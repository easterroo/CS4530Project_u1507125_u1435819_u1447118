package com.example.drawingapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Defines the app's navigation and routing between pages
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            LibraryScreen(
                onOpenDrawing = { id -> navController.navigate("draw/$id") },
                onNewDrawing = { navController.navigate("draw/new") }
            )
        }
        composable("draw/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            CanvasScreen()   // replace with your canvas screen
        }
    }
}