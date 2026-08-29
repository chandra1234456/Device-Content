package com.chandra.practice.deviceinfo.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.core.content.ContextCompat
import com.chandra.practice.deviceinfo.data.health.DeviceHealthCalculator
import com.chandra.practice.deviceinfo.data.model.BatteryDetails
import com.chandra.practice.deviceinfo.data.model.DeviceInfoItem
import com.chandra.practice.deviceinfo.data.model.HealthScore
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.data.model.NetworkTotalsSample
import com.chandra.practice.deviceinfo.data.model.QuickStat
import com.chandra.practice.deviceinfo.data.model.ThermalStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

/**
 * Collects device/hardware information. Every category is computed off the main thread and cached
 * in memory for the process lifetime (switching tabs is instant; [invalidate] forces a refresh).
 */
class DeviceInfoRepository(private val context: Context) {

    private val cache = mutableMapOf<InfoCategory, List<DeviceInfoItem>>()

    suspend fun getCategoryInfo(category: InfoCategory): List<DeviceInfoItem> {
        cache[category]?.let { return it }
        val items = withContext(Dispatchers.Default) { collect(category) }
        cache[category] = items
        return items
    }

    fun invalidate(category: InfoCategory? = null) {
        if (category == null) cache.clear() else cache.remove(category)
    }

    suspend fun getQuickStats(): List<QuickStat> = withContext(Dispatchers.Default) {
        val (ramUsed, ramTotal) = ramUsageMb()
        val ramRatio = if (ramTotal > 0) ramUsed.toFloat() / ramTotal else 0f
        val (storageUsed, storageTotal) = storageUsageBytes()
        val storageRatio = if (storageTotal > 0) storageUsed.toFloat() / storageTotal else 0f
        val batteryPercent = batteryPercent()

        listOf(
            QuickStat("RAM", "${(ramRatio * 100).toInt()}%", Icons.Filled.Memory, ramRatio),
            QuickStat("Storage", "${(storageRatio * 100).toInt()}%", Icons.Filled.Storage, storageRatio),
            QuickStat("Battery", "$batteryPercent%", Icons.Filled.BatteryFull, batteryPercent / 100f),
            QuickStat("CPU", "${Runtime.getRuntime().availableProcessors()} cores", Icons.Filled.DeveloperBoard, null),
            QuickStat("Network", networkType(), Icons.Filled.Wifi, null),
        )
    }

    suspend fun healthScore(): HealthScore = withContext(Dispatchers.Default) {
        DeviceHealthCalculator.calculate(
            battery = batteryDetails(),
            isLowMemory = isLowMemory(),
            storageUsageRatio = storageUsageRatio(),
            isNetworkConnected = isNetworkConnected(),
        )
    }

    private fun collect(category: InfoCategory): List<DeviceInfoItem> = try {
        when (category) {
            InfoCategory.OVERVIEW -> deviceInfo()
            InfoCategory.BATTERY -> batteryInfo()
            InfoCategory.DISPLAY -> screenInfo()
            InfoCategory.MEMORY_STORAGE -> memoryAndStorageInfo()
            InfoCategory.CPU_HARDWARE -> cpuAndHardwareInfo()
            InfoCategory.SENSORS -> sensorsInfo()
            InfoCategory.CAMERA -> cameraInfo()
            InfoCategory.NETWORK -> networkDetails()
            InfoCategory.APP_INFO -> appInfo()
            InfoCategory.LOCALE -> localeInfo()
        }
    } catch (e: Exception) {
        throw IllegalStateException("Couldn't read ${category.title}: ${e.message}", e)
    }

    private fun item(category: InfoCategory, label: String, value: String, copyable: Boolean = false) =
        DeviceInfoItem(category.title, label, value, copyable)

    // ---- Build & OS ----

    private fun deviceInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.OVERVIEW
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(Build.TIME))
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "Not available"

        return listOf(
            item(c, "Android ID", androidId(), copyable = true),
            item(c, "Model", Build.MODEL),
            item(c, "Manufacturer", Build.MANUFACTURER),
            item(c, "Brand", Build.BRAND),
            item(c, "Device", Build.DEVICE),
            item(c, "Product", Build.PRODUCT),
            item(c, "Board", Build.BOARD),
            item(c, "Hardware", Build.HARDWARE),
            item(c, "OS Version", Build.VERSION.RELEASE),
            item(c, "API Level", Build.VERSION.SDK_INT.toString()),
            item(c, "Build ID", Build.ID),
            item(c, "Build Time", buildTime),
            item(c, "Fingerprint", Build.FINGERPRINT, copyable = true),
            item(c, "Security Patch", securityPatch),
            item(c, "Bootloader", Build.BOOTLOADER),
            item(c, "Radio Version", Build.getRadioVersion() ?: "Unknown"),
            item(c, "Tags", Build.TAGS),
            item(c, "Type", Build.TYPE),
            item(c, "User", Build.USER),
            item(c, "Host", Build.HOST),
        )
    }

    private fun androidId(): String = try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ANDROID_ID"
    } catch (e: Exception) {
        "UNKNOWN_ANDROID_ID"
    }

    // ---- Battery ----

    private fun batteryInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.BATTERY
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPercent = (level * 100 / scale.toFloat()).toInt()

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val plug = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargePlug = chargeSourceLabel(plug)

        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val batteryHealth = batteryHealthLabel(health)

        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1

        return listOf(
            item(c, "Battery Level", "$batteryPercent%"),
            item(c, "Charging Status", if (isCharging) "Charging" else "Not Charging"),
            item(c, "Charge Source", chargePlug),
            item(c, "Battery Health", batteryHealth),
            item(c, "Temperature", "${temperature / 10f}°C"),
            item(c, "Technology", intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"),
            item(c, "Voltage", "${intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1} mV"),
        )
    }

    private fun batteryPercent(): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        return if (scale == 0) 0 else level * 100 / scale
    }

    private fun chargeSourceLabel(plug: Int): String = when (plug) {
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "Not charging"
    }

    private fun batteryHealthLabel(health: Int): String = when (health) {
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        else -> "Unknown"
    }

    /** Structured battery snapshot for the Battery screen and health score — polled, not cached. */
    fun batteryDetails(): BatteryDetails {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (scale == 0) 0 else (level * 100 / scale.toFloat()).toInt()

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val plug = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

        // BATTERY_PROPERTY_CURRENT_NOW is documented as microamps, but some OEMs report it in
        // milliamps directly, and its sign convention (charging vs. discharging) isn't reliable
        // across devices — direction comes from EXTRA_STATUS above, magnitude from this property.
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val rawCurrent = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        var currentMa = kotlin.math.abs(rawCurrent) / 1000
        if (currentMa < 5 && rawCurrent != 0) currentMa = kotlin.math.abs(rawCurrent)

        val voltageV = voltageMv / 1000f
        val powerW = (voltageV * currentMa) / 1000f

        return BatteryDetails(
            percent = percent,
            isCharging = isCharging,
            chargeSource = chargeSourceLabel(plug),
            health = batteryHealthLabel(health),
            technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown",
            temperatureC = temperature / 10f,
            voltageV = voltageV,
            currentMa = currentMa,
            powerW = powerW,
        )
    }

    // ---- Display ----

    private fun screenInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.DISPLAY
        val metrics = context.resources.displayMetrics
        val display = ContextCompat.getDisplayOrDefault(context)

        return listOf(
            item(c, "Resolution", "${metrics.widthPixels}x${metrics.heightPixels}"),
            item(c, "Density", String.format(Locale.getDefault(), "%.2f", metrics.density)),
            item(c, "Density DPI", "${metrics.densityDpi} dpi"),
            item(c, "Scaled Density", String.format(Locale.getDefault(), "%.2f", metrics.scaledDensity)),
            item(c, "XDPI", String.format(Locale.getDefault(), "%.2f", metrics.xdpi)),
            item(c, "YDPI", String.format(Locale.getDefault(), "%.2f", metrics.ydpi)),
            item(c, "Refresh Rate", "${display.refreshRate} Hz"),
            item(c, "Physical Size", screenSizeInInches(metrics)),
        )
    }

    private fun screenSizeInInches(metrics: android.util.DisplayMetrics): String {
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonal = sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
        return String.format(Locale.getDefault(), "%.1f\"", diagonal)
    }

    // ---- Memory & Storage ----

    private fun memoryAndStorageInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.MEMORY_STORAGE
        val memoryInfo = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        val usedRam = totalRam - availableRam
        val ramUsagePercent = (usedRam.toFloat() / totalRam.toFloat() * 100).toInt()

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorage = statFs.blockCountLong * statFs.blockSizeLong
        val availableStorage = statFs.availableBlocksLong * statFs.blockSizeLong
        val usedStorage = totalStorage - availableStorage
        val storageUsagePercent = (usedStorage.toFloat() / totalStorage.toFloat() * 100).toInt()

        return listOf(
            item(c, "Total RAM", formatBytes(totalRam)),
            item(c, "Available RAM", formatBytes(availableRam)),
            item(c, "Used RAM", formatBytes(usedRam)),
            item(c, "RAM Usage", "$ramUsagePercent%"),
            item(c, "Low Memory", if (memoryInfo.lowMemory) "Yes" else "No"),
            item(c, "Threshold", formatBytes(memoryInfo.threshold)),
            item(c, "Total Storage", formatBytes(totalStorage)),
            item(c, "Available Storage", formatBytes(availableStorage)),
            item(c, "Used Storage", formatBytes(usedStorage)),
            item(c, "Storage Usage", "$storageUsagePercent%"),
            item(c, "Is External Storage", if (Environment.isExternalStorageRemovable()) "Yes" else "No"),
            item(c, "External Storage State", Environment.getExternalStorageState()),
        )
    }

    private fun ramUsageMb(): Pair<Long, Long> {
        val memoryInfo = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memoryInfo)
        val used = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
        val total = memoryInfo.totalMem / (1024 * 1024)
        return used to total
    }

    private fun storageUsageBytes(): Pair<Long, Long> {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val total = statFs.totalBytes
        val used = total - statFs.availableBytes
        return used to total
    }

    /** 0f..1f fraction of storage currently used, for the health score. */
    fun storageUsageRatio(): Float {
        val (used, total) = storageUsageBytes()
        return if (total > 0) used.toFloat() / total else 0f
    }

    /** The OS's own signal that the system is memory-constrained — see [DeviceInfoRepository]
     *  callers for why this is preferred over a raw RAM usage percentage. */
    fun isLowMemory(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }

    /** 0f..1f fraction of RAM currently used, for the Live Monitor. */
    fun ramUsageRatio(): Float {
        val (used, total) = ramUsageMb()
        return if (total > 0) used.toFloat() / total else 0f
    }

    /**
     * The device's real-time thermal throttling state. There is no public, permission-free API
     * for system-wide CPU load on Android 8+ — SELinux blocks regular apps from reading
     * /proc/stat — so this (available since API 29) is used instead as an honest "is the device
     * running hot" signal for the Live Monitor rather than a fabricated CPU percentage.
     */
    fun thermalStatus(): ThermalStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalStatus.UNKNOWN
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return when (powerManager.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NONE
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatus.CRITICAL
            PowerManager.THERMAL_STATUS_EMERGENCY, PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.EMERGENCY
            else -> ThermalStatus.UNKNOWN
        }
    }

    /** Cumulative bytes sent/received since boot — [TrafficStats] only exposes running totals,
     *  so the Live Monitor computes a throughput rate itself from two samples over time. No
     *  permission required. */
    fun currentNetworkTotals(): NetworkTotalsSample = NetworkTotalsSample(
        timestampMs = System.currentTimeMillis(),
        rxBytes = TrafficStats.getTotalRxBytes(),
        txBytes = TrafficStats.getTotalTxBytes(),
    )

    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return String.format(Locale.getDefault(), "%.2f %s", size, units[unitIndex])
    }

    // ---- CPU & Hardware ----

    private fun cpuAndHardwareInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.CPU_HARDWARE
        val abis = Build.SUPPORTED_ABIS.joinToString(", ")
        val cpuInfo = try {
            val cpuInfoFile = File("/proc/cpuinfo")
            if (cpuInfoFile.exists()) {
                cpuInfoFile.readLines()
                    .firstOrNull { it.contains("model name") || it.contains("Processor") }
                    ?.substringAfter(":")?.trim() ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }

        return listOf(
            item(c, "Processor Cores", Runtime.getRuntime().availableProcessors().toString()),
            item(c, "CPU Info", cpuInfo),
            item(c, "Supported ABIs", abis),
            item(c, "Primary ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"),
            item(c, "Hardware", Build.HARDWARE),
            item(c, "Device", Build.DEVICE),
            item(c, "Board", Build.BOARD),
            item(c, "Product", Build.PRODUCT),
        )
    }

    // ---- Sensors ----

    private fun sensorsInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.SENSORS
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val sensorTypes = listOf(
            "Accelerometer" to Sensor.TYPE_ACCELEROMETER,
            "Gyroscope" to Sensor.TYPE_GYROSCOPE,
            "Proximity" to Sensor.TYPE_PROXIMITY,
            "Light" to Sensor.TYPE_LIGHT,
            "Magnetic Field" to Sensor.TYPE_MAGNETIC_FIELD,
            "Pressure" to Sensor.TYPE_PRESSURE,
            "Humidity" to Sensor.TYPE_RELATIVE_HUMIDITY,
            "Ambient Temperature" to Sensor.TYPE_AMBIENT_TEMPERATURE,
            "Step Counter" to Sensor.TYPE_STEP_COUNTER,
            "Heart Rate" to Sensor.TYPE_HEART_RATE,
        )

        val result = mutableListOf(item(c, "Total Sensors", sensors.size.toString()))
        sensorTypes.forEach { (name, type) ->
            val available = sensors.any { it.type == type }
            result += item(c, name, if (available) "Available" else "Not Available")
        }
        return result
    }

    /** Whether the device has a given sensor type at all — used by the Diagnostics hub's
     *  instant "Run All" check, before a user opens the live-reading test for that sensor. */
    fun hasSensor(sensorType: Int): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getDefaultSensor(sensorType) != null
    }

    // ---- Camera ----

    private fun cameraInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.CAMERA
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList
            val result = mutableListOf(item(c, "Total Cameras", cameraIds.size.toString()))
            cameraIds.forEachIndexed { index, cameraId ->
                val facing = when (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    else -> "External"
                }
                result += item(c, "Camera $index", facing)
            }
            result
        } catch (e: Exception) {
            listOf(item(c, "Camera Info", "Permission required or not available"))
        }
    }

    // ---- Network ----

    private fun networkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        return when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "Unknown"
        }
    }

    /** Connected AND validated (i.e. actually has working internet, not just an association). */
    fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }

    private fun networkDetails(): List<DeviceInfoItem> {
        val c = InfoCategory.NETWORK
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val wifiInfo = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.connectionInfo

        val result = mutableListOf(
            item(c, "Network Type", networkType()),
            item(c, "Is Connected", if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) "Yes" else "No"),
            item(c, "Is Metered", if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == false) "Yes" else "No"),
            item(c, "Is Validated", if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true) "Yes" else "No"),
        )

        if (wifiInfo != null) {
            result += item(c, "WiFi SSID", wifiInfo.ssid.replace("\"", ""))
            result += item(c, "WiFi BSSID", wifiInfo.bssid ?: "Unknown", copyable = true)
            result += item(c, "WiFi Signal", "${wifiInfo.rssi} dBm")
            result += item(c, "Link Speed", "${wifiInfo.linkSpeed} Mbps")
            result += item(c, "Frequency", "${wifiInfo.frequency} MHz")
        }

        result += item(c, "Local IP", localIpAddress(), copyable = true)
        return result
    }

    private fun localIpAddress(): String = try {
        NetworkInterface.getNetworkInterfaces().asSequence()
            .flatMap { it.inetAddresses.asSequence() }
            .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
            ?.hostAddress ?: "No IP found"
    } catch (e: Exception) {
        "Unavailable"
    }

    // ---- App Info ----

    private fun appInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.APP_INFO
        val pm = context.packageManager
        val pkg = context.packageName
        val info = pm.getPackageInfo(pkg, 0)
        val appName = context.applicationInfo.loadLabel(pm).toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }

        return listOf(
            item(c, "App Name", appName),
            item(c, "Package Name", pkg, copyable = true),
            item(c, "Version Name", info.versionName ?: "NA"),
            item(c, "Version Code", versionCode.toString()),
            item(c, "Install Time", dateFormat.format(Date(info.firstInstallTime))),
            item(c, "Update Time", dateFormat.format(Date(info.lastUpdateTime))),
            item(c, "Target SDK", "${info.applicationInfo?.targetSdkVersion ?: "NA"}"),
            item(c, "Min SDK", "${info.applicationInfo?.minSdkVersion ?: "NA"}"),
            item(c, "Process Name", info.applicationInfo?.processName ?: "NA"),
        )
    }

    // ---- Locale ----

    private fun localeInfo(): List<DeviceInfoItem> {
        val c = InfoCategory.LOCALE
        val locale = Locale.getDefault()
        return listOf(
            item(c, "Language", locale.language),
            item(c, "Country", locale.country),
            item(c, "Display Language", locale.displayLanguage),
            item(c, "Display Country", locale.displayCountry),
            item(c, "Display Name", locale.displayName),
            item(c, "Script", locale.script),
            item(c, "Variant", locale.variant),
        )
    }
}
