package com.chandra.practice.deviceinfo.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun getInstalledApps(context: Context): List<ApplicationInfo> {
    val pm = context.packageManager

    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter {
            pm.getLaunchIntentForPackage(it.packageName) != null
        }
}