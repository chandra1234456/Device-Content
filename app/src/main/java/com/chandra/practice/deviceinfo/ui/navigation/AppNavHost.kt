@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.chandra.practice.deviceinfo.ui.navigation

import android.app.Activity
import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
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
import com.chandra.practice.deviceinfo.update.UpdateCheckResult
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
    onCheckForUpdates: ((UpdateCheckResult) -> Unit) -> Unit = {},
    initialCategory: InfoCategory = InfoCategory.OVERVIEW,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity

    // On tablets, foldables and landscape, keep content at a readable width instead of stretching
    // edge to edge. Hardware tests that need the whole screen opt out.
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isFullScreenRoute = currentRoute == Routes.DISPLAY_TEST || currentRoute == Routes.TOUCH_TEST
    val widthModifier = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact || isFullScreenRoute) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxHeight().widthIn(max = 840.dp)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = widthModifier,
        enterTransition = { slideInHorizontally(tween(300)) { it / 6 } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it / 6 } + fadeOut(tween(200)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it / 6 } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it / 6 } + fadeOut(tween(200)) },
    ) {
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
                onOpenNetworkAnalyzer = { navController.navigate(Routes.NETWORK_ANALYZER) },
                onOpenSensorExplorer = { navController.navigate(Routes.SENSOR_EXPLORER) },
                onOpenBenchmark = { navController.navigate(Routes.BENCHMARK) },
                onOpenStorageAnalyzer = { navController.navigate(Routes.STORAGE_ANALYZER) },
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
                onBack = { navController.safePop() },
                onOpenPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) },
                onCheckForUpdates = onCheckForUpdates,
                onOpenTerms = { navController.navigate(Routes.TERMS) },
                viewModel = viewModel(factory = SettingsViewModel.Factory(appContainer.userPreferencesRepository)),
            )
        }
        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onBack = { navController.safePop() },
                onViewOnline = { navController.navigate(Routes.webView(PRIVACY_POLICY_URL, "Privacy Policy")) },
            )
        }
        composable(Routes.TERMS) {
            TermsScreen(
                onBack = { navController.safePop() },
                onViewOnline = { navController.navigate(Routes.webView(TERMS_URL, "Terms & Services")) },
            )
        }
        composable(Routes.BATTERY) {
            BatteryScreen(
                onBack = { navController.safePop() },
                viewModel = viewModel(factory = BatteryViewModel.Factory(appContainer.deviceInfoRepository)),
            )
        }
        composable(Routes.MONITOR) {
            MonitorScreen(
                onBack = { navController.safePop() },
                viewModel = viewModel(factory = MonitorViewModel.Factory(appContainer.deviceInfoRepository)),
            )
        }
        composable(Routes.DIAGNOSTICS) {
            DiagnosticsScreen(
                onBack = { navController.safePop() },
                onOpenTest = { testId ->
                    val route = when (testId) {
                        DiagnosticTestId.TOUCH -> Routes.TOUCH_TEST
                        DiagnosticTestId.VIBRATION -> Routes.VIBRATION_TEST
                        DiagnosticTestId.FLASH -> Routes.FLASH_TEST
                        DiagnosticTestId.DISPLAY -> Routes.DISPLAY_TEST
                        DiagnosticTestId.SPEAKER -> Routes.SPEAKER_TEST
                        DiagnosticTestId.MICROPHONE -> Routes.MIC_TEST
                        DiagnosticTestId.CAMERA -> Routes.CAMERA_TEST
                        DiagnosticTestId.GPS -> Routes.LOCATION_TEST
                        else -> Routes.sensorTest(testId)
                    }
                    navController.navigate(route)
                },
                onOpenCheckup = { navController.navigate(Routes.PHONE_CHECKUP) },
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
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.TOUCH, result) },
            )
        }
        composable(Routes.VIBRATION_TEST) {
            VibrationTestScreen(
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.VIBRATION, result) },
            )
        }
        composable(Routes.FLASH_TEST) {
            FlashTestScreen(
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.FLASH, result) },
            )
        }
        composable(Routes.DISPLAY_TEST) {
            DisplayTestScreen(
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.DISPLAY, result) },
            )
        }
        composable(Routes.SPEAKER_TEST) {
            com.chandra.practice.deviceinfo.ui.screens.diagnostics.SpeakerTestScreen(
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.SPEAKER, result) },
            )
        }
        composable(Routes.MIC_TEST) {
            com.chandra.practice.deviceinfo.ui.screens.diagnostics.MicrophoneTestScreen(
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.MICROPHONE, result) },
            )
        }
        composable(Routes.CAMERA_TEST) {
            com.chandra.practice.deviceinfo.ui.screens.diagnostics.CameraTestScreen(
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.CAMERA, result) },
            )
        }
        composable(Routes.LOCATION_TEST) {
            com.chandra.practice.deviceinfo.ui.screens.diagnostics.LocationTestScreen(
                onBack = { navController.safePop() },
                onResult = { result -> appContainer.diagnosticsResultsRepository.setResult(DiagnosticTestId.GPS, result) },
            )
        }
        composable(Routes.PHONE_CHECKUP) {
            val diagViewModel: DiagnosticsViewModel = viewModel(
                factory = DiagnosticsViewModel.Factory(
                    appContainer.deviceInfoRepository,
                    appContainer.diagnosticsResultsRepository,
                ),
            )
            val diagState by diagViewModel.uiState.collectAsStateWithLifecycle()
            com.chandra.practice.deviceinfo.ui.screens.diagnostics.PhoneCheckupScreen(
                tests = diagState.tests,
                onRunAutoChecks = { diagViewModel.runAutomaticChecks() },
                onBack = { navController.safePop() },
            )
        }
        composable(Routes.NETWORK_ANALYZER) {
            val netVm = viewModel<com.chandra.practice.deviceinfo.ui.screens.network.NetworkViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.chandra.practice.deviceinfo.ui.screens.network.NetworkViewModel(appContainer.deviceInfoRepository) as T
                    }
                },
            )
            com.chandra.practice.deviceinfo.ui.screens.network.NetworkAnalyzerScreen(
                viewModel = netVm,
                onBack = { navController.safePop() },
            )
        }
        composable(Routes.SENSOR_EXPLORER) {
            val sensorVm = viewModel<com.chandra.practice.deviceinfo.ui.screens.sensors.SensorExplorerViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.chandra.practice.deviceinfo.ui.screens.sensors.SensorExplorerViewModel(appContainer.sensorExplorerRepository) as T
                    }
                },
            )
            com.chandra.practice.deviceinfo.ui.screens.sensors.SensorExplorerScreen(
                viewModel = sensorVm,
                onBack = { navController.safePop() },
            )
        }
        composable(Routes.BENCHMARK) {
            val benchVm = viewModel<com.chandra.practice.deviceinfo.ui.screens.benchmark.BenchmarkViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.chandra.practice.deviceinfo.ui.screens.benchmark.BenchmarkViewModel(appContainer.benchmarkRepository) as T
                    }
                },
            )
            com.chandra.practice.deviceinfo.ui.screens.benchmark.BenchmarkScreen(
                viewModel = benchVm,
                onBack = { navController.safePop() },
            )
        }
        composable(Routes.STORAGE_ANALYZER) {
            val storageVm = viewModel<com.chandra.practice.deviceinfo.ui.screens.storage.StorageViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.chandra.practice.deviceinfo.ui.screens.storage.StorageViewModel(appContainer.deviceInfoRepository) as T
                    }
                },
            )
            com.chandra.practice.deviceinfo.ui.screens.storage.StorageAnalyzerScreen(
                viewModel = storageVm,
                onBack = { navController.safePop() },
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
                onBack = { navController.safePop() },
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
            WebViewScreen(url = url, title = title, onBack = { navController.safePop() })
        }
    }
    }
}

/**
 * Pops only when the current screen is fully resumed. A fast double-tap on a back arrow (or Back
 * during a transition) would otherwise pop twice and leave the user on a blank NavHost.
 */
private fun NavController.safePop() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) popBackStack()
}
