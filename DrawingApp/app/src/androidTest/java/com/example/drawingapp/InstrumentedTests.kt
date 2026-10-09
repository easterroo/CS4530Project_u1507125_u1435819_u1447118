package com.example.drawingapp

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class InstrumentedTests {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.drawingapp", appContext.packageName)
    }

    @Test
    fun testNewDrawingSelect() {
       // TODO: Test to make sure that a new fresh canvas opens when selecting "new drawing"
    }

    @Test
    fun testDrawingSelect() {
        // TODO: Test to make sure redirects to canvas when selecting a drawing
    }
}

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
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            AppNavigation(navController = navController)
        }
    }

    @Test
    fun testStartOnLibraryScreen() {
        assertEquals("library", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun testNewDrawingButtonOnLibraryScreen() {
        composeTestRule.onNodeWithText("New Drawing").performClick()
        composeTestRule.waitForIdle()

        val entry = navController.currentBackStackEntry
        assertEquals("draw/{id}", entry?.destination?.route)
        assertEquals("new", entry?.arguments?.getString("id"))
    }
}