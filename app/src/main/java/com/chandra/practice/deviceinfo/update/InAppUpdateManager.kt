package com.chandra.practice.deviceinfo.update

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Outcome of an update check, used to give feedback after a manual "Check for updates". */
enum class UpdateCheckResult { UP_TO_DATE, UPDATE_STARTED, READY_TO_INSTALL, UNAVAILABLE }

/**
 * Wraps Play Core's in-app update API.
 *
 * - Normal updates download in the background (flexible); [isUpdateReady] flips once finished so
 *   the UI can offer a one-tap restart.
 * - Updates with a Play Console in-app update priority of [IMMEDIATE_PRIORITY] or higher use the
 *   full-screen immediate flow, so critical fixes can't be skipped.
 * - If the user dismisses the flexible prompt, it isn't shown again until the next app launch,
 *   unless they ask via [checkForUpdate] with `userInitiated = true`.
 *
 * No-op on installs Play Core can't see an update for (debug builds, sideloads, unpublished apps),
 * so it's safe to always call [start]. Must be constructed from onCreate, because the result
 * launcher has to be registered before the activity is STARTED.
 */
class InAppUpdateManager(private val activity: ComponentActivity) {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)

    private val _isUpdateReady = MutableStateFlow(false)
    val isUpdateReady: StateFlow<Boolean> = _isUpdateReady

    /** 0f..1f while a flexible update downloads, null otherwise. */
    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress

    private var declinedThisSession = false

    private val updateFlowLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Update flow did not complete (result code ${result.resultCode})")
            if (result.resultCode == Activity.RESULT_CANCELED) declinedThisSession = true
        }
    }

    private val installListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val total = state.totalBytesToDownload()
                _downloadProgress.value = if (total > 0) state.bytesDownloaded().toFloat() / total else null
            }
            InstallStatus.DOWNLOADED -> {
                _downloadProgress.value = null
                _isUpdateReady.value = true
            }
            InstallStatus.FAILED, InstallStatus.CANCELED -> _downloadProgress.value = null
            else -> Unit
        }
    }

    fun start() {
        appUpdateManager.registerListener(installListener)
        checkForUpdate()
    }

    /**
     * Call from onResume as well. A flexible download can finish while backgrounded, and an
     * immediate update that was interrupted must be resumed.
     */
    fun checkForUpdate(userInitiated: Boolean = false, onResult: (UpdateCheckResult) -> Unit = {}) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info -> onResult(handle(info, userInitiated)) }
            .addOnFailureListener { e ->
                Log.d(TAG, "Update check failed", e)
                onResult(UpdateCheckResult.UNAVAILABLE)
            }
    }

    private fun handle(info: AppUpdateInfo, userInitiated: Boolean): UpdateCheckResult {
        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            _isUpdateReady.value = true
            return UpdateCheckResult.READY_TO_INSTALL
        }
        if (info.installStatus() == InstallStatus.DOWNLOADING) return UpdateCheckResult.UPDATE_STARTED

        // An immediate update the user left mid-way must be continued.
        if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            return startFlow(info, AppUpdateType.IMMEDIATE)
        }
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return UpdateCheckResult.UP_TO_DATE

        return when {
            info.updatePriority() >= IMMEDIATE_PRIORITY && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ->
                startFlow(info, AppUpdateType.IMMEDIATE)
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) && (userInitiated || !declinedThisSession) ->
                startFlow(info, AppUpdateType.FLEXIBLE)
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) && userInitiated ->
                startFlow(info, AppUpdateType.IMMEDIATE)
            else -> UpdateCheckResult.UP_TO_DATE
        }
    }

    private fun startFlow(info: AppUpdateInfo, type: Int): UpdateCheckResult {
        val started = runCatching {
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateFlowLauncher,
                AppUpdateOptions.newBuilder(type).build(),
            )
        }.onFailure { Log.d(TAG, "Couldn't start update flow", it) }.isSuccess
        return if (started) UpdateCheckResult.UPDATE_STARTED else UpdateCheckResult.UNAVAILABLE
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun stop() {
        appUpdateManager.unregisterListener(installListener)
    }

    private companion object {
        const val TAG = "InAppUpdateManager"

        /** Set the in-app update priority (0-5) per release in Play Console; 4+ forces the update. */
        const val IMMEDIATE_PRIORITY = 4
    }
}
