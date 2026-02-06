package com.chandra.practice.deviceinfo.data

    data class QuickStat(
        val label: String,
        val value: String,
        val iconRes: Int,
        val description: String = ""
    )