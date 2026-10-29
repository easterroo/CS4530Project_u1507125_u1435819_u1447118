package com.example.drawingapp

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.printToLog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class NavigationInstrumentedTest {
    @get: Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            val canvasViewModel: CanvasViewModel = viewModel(factory = DrawingViewModelProvider.Factory)
            val libraryViewModel: LibraryViewModel = viewModel(factory = DrawingViewModelProvider.Factory)
            AppNavigation(
                navController = navController,
                canvasViewModel = canvasViewModel,
                libraryViewModel = libraryViewModel
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testStartOnLibraryScreen() {
        assertEquals("library", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun testNewDrawingButtonOnLibraryScreen() {
        composeTestRule.onNodeWithText("New Drawing").performClick()
        composeTestRule.waitForIdle()
        assertEquals("draw", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun testEndToEndDrawingCreation() {
        composeTestRule.onNodeWithText("New Drawing").performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("My Drawings").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText("Untitled Drawing").performClick()
        composeTestRule.onNodeWithTag("titleField").performTextReplacement("My Art")
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.onNodeWithContentDescription("Save").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("My Art").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("My Art").assertIsDisplayed()
    }
}