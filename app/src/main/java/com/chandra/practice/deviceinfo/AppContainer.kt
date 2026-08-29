package com.chandra.practice.deviceinfo

import android.content.Context
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository
import com.chandra.practice.deviceinfo.data.repository.DiagnosticsResultsRepository
import com.chandra.practice.deviceinfo.data.repository.UserPreferencesRepository

/** Small hand-rolled service locator — this app is far too small to justify a DI framework. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val deviceInfoRepository = DeviceInfoRepository(appContext)
    val userPreferencesRepository = UserPreferencesRepository(appContext)
    val diagnosticsResultsRepository = DiagnosticsResultsRepository()
}
