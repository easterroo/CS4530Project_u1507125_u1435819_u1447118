package com.example.drawingapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

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
class ExampleInstrumentedTest {
    @get: Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCounter(){
        composeTestRule.setContent { Counter() }
        composeTestRule.onNodeWithTag("counterDisplay").assertTextEquals("Count: 0")
        composeTestRule.onNodeWithText("Add Count").performClick()
        composeTestRule.onNodeWithTag("counterDisplay").assertTextEquals("Count: 1")

    }

    @Test
    fun testDisappearingSection(){
        composeTestRule.setContent { DisappearingSection() }
        composeTestRule.onNodeWithText("Testing Demo").assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(2500)
        composeTestRule.onNodeWithText("Testing Demo").assertDoesNotExist()
    }
}