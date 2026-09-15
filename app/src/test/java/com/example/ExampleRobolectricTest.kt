package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Reon Music", appName)
  }

  @Test
  fun `main activity displays home screen initially`() {
    composeTestRule.onNodeWithTag("reon_home_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("floating_mini_player").assertIsDisplayed()
  }

  @Test
  fun `expand now playing from mini player and return to home screen`() {
    // 1. Initial state is Home Screen
    composeTestRule.onNodeWithTag("reon_home_screen").assertIsDisplayed()

    // 2. Tap floating mini player track info to open Now Playing
    composeTestRule.onNodeWithTag("mini_track_info").performClick()
    composeTestRule.waitForIdle()

    // 3. Verify Now Playing screen is visible
    composeTestRule.onNodeWithTag("now_playing_screen").assertIsDisplayed()

    // 4. Tap back button to minimize and return to Home Screen
    composeTestRule.onNodeWithTag("back_button").performClick()
    composeTestRule.waitForIdle()

    // 5. Verify Home Screen is visible again
    composeTestRule.onNodeWithTag("reon_home_screen").assertIsDisplayed()
  }
}


