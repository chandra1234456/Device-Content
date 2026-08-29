package com.chandra.practice.deviceinfo.update

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Wraps Play Core's in-app update API. Silently starts a background (flexible) download the
 * moment a newer version is live on the Play Store, then flips [isUpdateReady] once it has
 * finished downloading so the UI can offer a one-tap restart.
 *
 * This is a no-op on any install Play Core can't see an update for — debug builds, sideloaded
 * APKs, or before this app has ever been published — so it's safe to always call [start].
 */
class InAppUpdateManager(private val activity: ComponentActivity) {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)

    private val _isUpdateReady = MutableStateFlow(false)
    val isUpdateReady: StateFlow<Boolean> = _isUpdateReady

    // Must be registered before the activity reaches STARTED, so this class is only ever
    // constructed from onCreate.
    private val updateFlowLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.d("InAppUpdateManager", "Update flow did not complete (result code ${result.resultCode})")
        }
    }

    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            _isUpdateReady.value = true
        }
    }

    fun start() {
        appUpdateManager.registerListener(installListener)
        checkForUpdate()
    }

    /** Call again from onResume — a flexible download can finish while the app is backgrounded. */
    fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> _isUpdateReady.value = true
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateFlowLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    )
                }
            }
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun stop() {
        appUpdateManager.unregisterListener(installListener)
    }
}
