/*
 * REON Music App
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.reon.music.ui.ReonApp
import com.reon.music.ui.theme.ReonTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main Activity - Entry point for the UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Playback/sync notifications are dropped on Android 13+ without this.
    // Denial is non-fatal: notifications simply stay silent.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    companion object {
        // Minimum time the splash is kept visible for branding.
        // NOTE: this uses a suspending delay on a background coroutine — it
        // never blocks the main thread, so it cannot cause an ANR.
        private const val SPLASH_HOLD_MS = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen with 2-second delay
        val splashScreen = installSplashScreen()

        var keepSplashOnScreen = true

        // Keep splash screen visible for 2 seconds
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // Launch coroutine to dismiss splash after 2 seconds
        lifecycleScope.launch {
            delay(SPLASH_HOLD_MS) // 2 seconds
            keepSplashOnScreen = false
        }
        
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        setContent {
            ReonApp()
        }
    }
}