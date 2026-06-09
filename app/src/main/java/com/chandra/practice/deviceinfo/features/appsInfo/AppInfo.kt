package com.chandra.practice.deviceinfo.features.appsInfo

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val version: String,
    val size: String,
    val sizeBytes: Long,
    val updateTime: Long,
    val installTime: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val isSystemApp: Boolean
)