package com.chandra.practice.deviceinfo.features.appsInfo

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val version: String,
    val size: String,
    val isSystemApp: Boolean
)