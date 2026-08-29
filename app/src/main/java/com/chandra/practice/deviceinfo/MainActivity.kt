@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.chandra.practice.deviceinfo

import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.data.repository.UserPreferences
import com.chandra.practice.deviceinfo.ui.navigation.AppNavHost
import com.chandra.practice.deviceinfo.ui.navigation.Routes
import com.chandra.practice.deviceinfo.ui.theme.DeviceInfoTheme
import com.chandra.practice.deviceinfo.ui.theme.rememberIsDarkTheme
import com.chandra.practice.deviceinfo.update.InAppUpdateManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val appContainer = AppContainer(applicationContext)
        inAppUpdateManager = InAppUpdateManager(this)
        inAppUpdateManager.start()

        // Set by a static launcher shortcut (res/xml/shortcuts.xml) to jump straight to a category.
        val initialCategory = intent.getStringExtra("shortcut_category")
            ?.let { name -> runCatching { InfoCategory.valueOf(name) }.getOrNull() }
            ?: InfoCategory.OVERVIEW

        var startDestination by mutableStateOf<String?>(null)
        splashScreen.setKeepOnScreenCondition { startDestination == null }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.iconView.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .alpha(0f)
                .setDuration(350L)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { splashScreenView.remove() }
                .start()
        }

        lifecycleScope.launch {
            val hasSeenIntro = appContainer.userPreferencesRepository.preferences.first().hasSeenIntro
            startDestination = if (hasSeenIntro) Routes.HOME else Routes.INTRO
        }

        setContent {
            val destination = startDestination ?: return@setContent
            val preferences by appContainer.userPreferencesRepository.preferences
                .collectAsStateWithLifecycle(initialValue = UserPreferences())
            val darkTheme = rememberIsDarkTheme(preferences.themeMode)
            val isUpdateReady by inAppUpdateManager.isUpdateReady.collectAsStateWithLifecycle()

            DeviceInfoTheme(darkTheme = darkTheme, dynamicColor = preferences.dynamicColor) {
                val windowSizeClass = calculateWindowSizeClass(this)
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavHost(
                        windowSizeClass = windowSizeClass,
                        appContainer = appContainer,
                        startDestination = destination,
                        isUpdateReady = isUpdateReady,
                        onRestartToUpdate = inAppUpdateManager::completeUpdate,
                        initialCategory = initialCategory,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.checkForUpdate()
        }
    }

    override fun onDestroy() {
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.stop()
        }
        super.onDestroy()
    }
}
