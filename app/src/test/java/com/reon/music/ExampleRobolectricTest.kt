package com.reon.music

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.reon.music.ui.HomeViewModel
import com.reon.music.ui.theme.AccentColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

  @Test
  fun `verify theme mode state transition in viewmodel`() {
    val viewModel = HomeViewModel(ApplicationProvider.getApplicationContext<ReonApplication>())
    
    // Default theme is LIGHT
    assertEquals("LIGHT", viewModel.uiState.value.themeMode)

    // Switch to DARK
    viewModel.setThemeMode("DARK")
    assertEquals("DARK", viewModel.uiState.value.themeMode)
    assertEquals("Theme set to Dark", viewModel.uiState.value.toastMessage)

    // Switch to OLED
    viewModel.setThemeMode("OLED")
    assertEquals("OLED", viewModel.uiState.value.themeMode)
    assertEquals("Theme set to Oled", viewModel.uiState.value.toastMessage)
  }

  @Test
  fun `verify accent color customization in viewmodel`() {
    val viewModel = HomeViewModel(ApplicationProvider.getApplicationContext<ReonApplication>())

    // Default accent index is 0
    assertEquals(0, viewModel.uiState.value.accentColorIndex)

    // Select index 1 (Neon Cyan)
    viewModel.setAccentColor(1)
    assertEquals(1, viewModel.uiState.value.accentColorIndex)
    assertEquals("Accent color changed to Neon Cyan", viewModel.uiState.value.toastMessage)

    // Select index 2 (Cyber Purple)
    viewModel.setAccentColor(2)
    assertEquals(2, viewModel.uiState.value.accentColorIndex)
    assertEquals("Accent color changed to Cyber Purple", viewModel.uiState.value.toastMessage)
  }

  @Test
  fun `verify font family and font size scale changes`() {
    val viewModel = HomeViewModel(ApplicationProvider.getApplicationContext<ReonApplication>())

    // Default values
    assertEquals("SANS_SERIF", viewModel.uiState.value.fontFamilyChoice)
    assertEquals(1.0f, viewModel.uiState.value.fontSizeScale)

    // Change Font to MONOSPACE
    viewModel.setFontFamily("MONOSPACE")
    assertEquals("MONOSPACE", viewModel.uiState.value.fontFamilyChoice)
    assertEquals("Font family set to Monospace (Technical)", viewModel.uiState.value.toastMessage)

    // Scale font to 1.1x
    viewModel.setFontSizeScale(1.1f)
    assertEquals(1.1f, viewModel.uiState.value.fontSizeScale)
    assertEquals("Text scale set to 110%", viewModel.uiState.value.toastMessage)
  }

  @Test
  fun `verify user profile editing updates state successfully`() {
    val viewModel = HomeViewModel(ApplicationProvider.getApplicationContext<ReonApplication>())

    // Verify initial values
    assertEquals("Rohith", viewModel.uiState.value.greetingName)
    assertEquals("Audiophile & 192kHz Hi-Res Enthusiast", viewModel.uiState.value.userProfileBio)

    // Update profile
    viewModel.updateProfile("Rayan", "Hi-Res Master Engineer & DSD collector")
    assertEquals("Rayan", viewModel.uiState.value.greetingName)
    assertEquals("Hi-Res Master Engineer & DSD collector", viewModel.uiState.value.userProfileBio)
    assertEquals("Profile updated for Rayan", viewModel.uiState.value.toastMessage)
  }

  @Test
  fun `verify notification unread badging and clearance logic`() {
    val viewModel = HomeViewModel(ApplicationProvider.getApplicationContext<ReonApplication>())

    // Wait for seeding and load
    var attempts = 0
    while (viewModel.uiState.value.notifications.isEmpty() && attempts < 50) {
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        Thread.sleep(50)
        attempts++
    }

    // Initially we have some notifications loaded
    assertTrue(viewModel.uiState.value.notifications.isNotEmpty())
    
    val unreadCount = viewModel.uiState.value.notifications.count { !it.isRead }
    assertTrue(unreadCount > 0)

    // Mark all as read
    viewModel.markAllNotificationsAsRead()
    attempts = 0
    while (viewModel.uiState.value.notifications.any { !it.isRead } && attempts < 50) {
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        Thread.sleep(50)
        attempts++
    }

    val remainingUnread = viewModel.uiState.value.notifications.count { !it.isRead }
    assertEquals(0, remainingUnread)
    assertEquals("All notifications marked as read", viewModel.uiState.value.toastMessage)

    // Clear all
    viewModel.clearAllNotifications()
    attempts = 0
    while (viewModel.uiState.value.notifications.isNotEmpty() && attempts < 50) {
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        Thread.sleep(50)
        attempts++
    }

    assertTrue(viewModel.uiState.value.notifications.isEmpty())
    assertEquals("Notification feed cleared", viewModel.uiState.value.toastMessage)
  }

  @Test
  fun `verify cache clearance and thumbnail optimization`() {
    val viewModel = HomeViewModel(ApplicationProvider.getApplicationContext<ReonApplication>())

    // Verify initial storage used is 8600.0 MB
    assertEquals(8600f, viewModel.uiState.value.storageUsedMb)

    // Clear cache
    viewModel.clearCache()
    assertEquals(1200f, viewModel.uiState.value.storageUsedMb)
    assertEquals("Recovered 7.4 GB of lossless hi-res cached audio buffer", viewModel.uiState.value.toastMessage)

    // Optimize thumbnails
    viewModel.optimizeThumbnails()
    assertEquals("Compressed artwork cache footprints by 65%", viewModel.uiState.value.toastMessage)
  }
}



