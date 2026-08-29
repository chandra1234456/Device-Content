@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.chandra.practice.deviceinfo.ui.navigation

import android.app.Activity
import android.net.Uri
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chandra.practice.deviceinfo.AppContainer
import com.chandra.practice.deviceinfo.data.model.DiagnosticTestId
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.ui.screens.battery.BatteryScreen
import com.chandra.practice.deviceinfo.ui.screens.battery.BatteryViewModel
import com.chandra.practice.deviceinfo.ui.screens.diagnostics.DiagnosticsScreen
import com.chandra.practice.deviceinfo.ui.screens.diagnostics.DiagnosticsViewModel
import com.chandra.practice.deviceinfo.ui.screens.diagnostics.DisplayTestScreen
import com.chandra.practice.deviceinfo.ui.screens.diagnostics.FlashTestScreen
import com.chandra.practice.deviceinfo.ui.screens.diagnostics.SensorTestScreen
import com.chandra.practice.deviceinfo.ui.screens.diagnostics.TouchTestScreen
import com.chandra.practice.deviceinfo.ui.screens.diagnostics.VibrationTestScreen
import com.chandra.practice.deviceinfo.ui.screens.home.HomeScreen
import com.chandra.practice.deviceinfo.ui.screens.home.HomeViewModel
import com.chandra.practice.deviceinfo.ui.screens.intro.IntroScreen
import com.chandra.practice.deviceinfo.ui.screens.legal.PrivacyPolicyScreen
import com.chandra.practice.deviceinfo.ui.screens.legal.TermsScreen
import com.chandra.practice.deviceinfo.ui.screens.monitor.MonitorScreen
import com.chandra.practice.deviceinfo.ui.screens.monitor.MonitorViewModel
import com.chandra.practice.deviceinfo.ui.screens.settings.SettingsScreen
import com.chandra.practice.deviceinfo.ui.screens.settings.SettingsViewModel
import com.chandra.practice.deviceinfo.ui.screens.webview.WebViewScreen
import kotlinx.coroutines.launch

private const val PRIVACY_POLICY_URL = "https://sites.google.com/view/device-content/home"
private const val TERMS_URL = "https://sites.google.com/view/devicecontent/home"

@Composable
fun AppNavHost(
    windowSizeClass: WindowSizeClass,
    appContainer: AppContainer,
    startDestination: String,
    isUpdateReady: Boolean = false,
    onRestartToUpdate: () -> Unit = {},
    initialCategory: InfoCategory = InfoCategory.OVERVIEW,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.INTRO) {
            IntroScreen(
                onAgree = {
                    coroutineScope.launch {
                        appContainer.userPreferencesRepository.setHasSeenIntro(true)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.INTRO) { inclusive = true }
                        }
                    }
                },
                onExit = { activity?.finish() },
                onOpenPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) },
                onOpenTerms = { navController.navigate(Routes.TERMS) },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenBattery = { navController.navigate(Routes.BATTERY) },
                onOpenDiagnostics = { navController.navigate(Routes.DIAGNOSTICS) },
                onOpenMonitor = { navController.navigate(Routes.MONITOR) },
                isUpdateReady = isUpdateReady,
                onRestartToUpdate = onRestartToUpdate,
                viewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        appContainer.deviceInfoRepository,
                        initialCategory,
                    ),
                ),
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) },
                onOpenTerms = { navController.navigate(Routes.TERMS) },
                viewModel = viewModel(factory = SettingsViewModel.Factory(appContainer.userPreferencesRepository)),
            )
        }
        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onBack = { navController.popBackStack() },
                onViewOnline = { navController.navigate(Routes.webView(PRIVACY_POLICY_URL, "Privacy Policy")) },
            )
        }
        composable(Routes.TERMS) {
            TermsScreen(
                onBack = { navController.popBackStack() },
                onViewOnline = { navController.navigate(Routes.webView(TERMS_URL, "Terms & Services")) },
            )
        }
        composable(Routes.BATTERY) {
            BatteryScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel(factory = BatteryViewModel.Factory(appContainer.deviceInfoRepository)),
            )
        }
        composable(Routes.MONITOR) {
            MonitorScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel(factory = MonitorViewModel.Factory(appContainer.deviceInfoRepository)),
            )
        }
        composable(Routes.DIAGNOSTICS) {
            DiagnosticsScreen(
                onBack = { navController.popBackStack() },
                onOpenTest = { testId ->
                    val route = when (testId) {
                        DiagnosticTestId.TOUCH -> Routes.TOUCH_TEST
                        DiagnosticTestId.VIBRATION -> Routes.VIBRATION_TEST
                        DiagnosticTestId.FLASH -> Routes.FLASH_TEST
                        DiagnosticTestId.DISPLAY -> Routes.DISPLAY_TEST
                        else -> Routes.sensorTest(testId)
                    }
                    navController.navigate(route)
                },
                viewModel = viewModel(
                    factory = DiagnosticsViewModel.Factory(
                        appContainer.deviceInfoRepository,
                        appContainer.diagnosticsResultsRepository,
                    ),
                ),
            )
        }
        composable(Routes.TOUCH_TEST) {
            TouchTestScreen(
                onBack = { navController.popBackStack() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.TOUCH, result) },
            )
        }
        composable(Routes.VIBRATION_TEST) {
            VibrationTestScreen(
                onBack = { navController.popBackStack() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.VIBRATION, result) },
            )
        }
        composable(Routes.FLASH_TEST) {
            FlashTestScreen(
                onBack = { navController.popBackStack() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.FLASH, result) },
            )
        }
        composable(Routes.DISPLAY_TEST) {
            DisplayTestScreen(
                onBack = { navController.popBackStack() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.DISPLAY, result) },
            )
        }
        composable(
            route = Routes.SENSOR_TEST,
            arguments = listOf(navArgument("testId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getString("testId")
                ?.let { runCatching { DiagnosticTestId.valueOf(it) }.getOrNull() }
                ?: DiagnosticTestId.ACCELEROMETER
            SensorTestScreen(
                testId = testId,
                onBack = { navController.popBackStack() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(testId, result) },
            )
        }
        composable(
            route = Routes.WEBVIEW,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val url = Uri.decode(backStackEntry.arguments?.getString("url").orEmpty())
            val title = Uri.decode(backStackEntry.arguments?.getString("title").orEmpty())
            WebViewScreen(url = url, title = title, onBack = { navController.popBackStack() })
        }
    }
}
