package com.chandra.practice.deviceinfo.presentation

import android.app.ActivityManager
import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.adapter.DeviceInfoAdapter
import com.chandra.practice.deviceinfo.adapter.HorizontalButtonAdapter
import com.chandra.practice.deviceinfo.adapter.QuickStatsAdapter
import com.chandra.practice.deviceinfo.data.QuickStat
import com.chandra.practice.deviceinfo.data.DeviceInfoItem
import com.chandra.practice.deviceinfo.databinding.FragmentDeviceBinding
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class DeviceFragment : Fragment() {

    private lateinit var deviceBinding: FragmentDeviceBinding
    private lateinit var adapter: HorizontalButtonAdapter
    private  var deviceInfoAdapter: DeviceInfoAdapter? = null
    private var currentCategory = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        deviceBinding = FragmentDeviceBinding.inflate(layoutInflater)
        return deviceBinding.root
    }



    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupSearchFunctionality()
        setupThemeToggle()
        setupSwipeRefresh()
        setupQuickStats()
        setupFabExport()
        setupFilterChip()

        val infoOptions = listOf(
            "Network Type",
            "Battery Info",
            "Get Locale",
            "Screen Info",
            "Get App Info",
            "Get Build and OS Info",
            "Memory & Storage",
            "CPU & Hardware",
            "Sensors",
            "Camera Info",
            "Network Details"
        )

        adapter = HorizontalButtonAdapter(infoOptions) { clickedItem ->
            showInfo(clickedItem)
            currentCategory = clickedItem
        }
        deviceBinding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        // Load default info on start
        val defaultCategory = "Get Build and OS Info"
        showInfo(defaultCategory)
        currentCategory = defaultCategory

        deviceBinding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        deviceBinding.horizontalRecyclerView.adapter = adapter

        // Setup main recycler view
        deviceBinding.deviceInfoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        deviceInfoAdapter = DeviceInfoAdapter(emptyList()) { item ->
            copyToClipboard(item.label, item.value)
        }
        deviceBinding.deviceInfoRecyclerView.adapter = deviceInfoAdapter
    }

    // ====== Setup Functions ======

    private fun setupMenu() {
        setHasOptionsMenu(true)
    }

    private fun setupSearchFunctionality() {
        // Search functionality will be in the action bar
    }

    private fun setupThemeToggle() {
        deviceBinding.themeToggleButton.setOnClickListener {
            toggleTheme()
        }
        updateThemeIcon()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setupSwipeRefresh() {
        deviceBinding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
            deviceBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupQuickStats() {
        val quickStats = listOf(
            QuickStat("RAM", getCurrentRamUsage(), R.drawable.ic_memory),
            QuickStat("Storage", getStorageUsage(), R.drawable.ic_storage),
            QuickStat("Battery", getBatteryPercentage(), R.drawable.ic_battery),
            QuickStat("CPU", "${Runtime.getRuntime().availableProcessors()} Cores", R.drawable.ic_cpu),
            QuickStat("Network", getNetworkType(), R.drawable.ic_network_wifi)
        )

        val quickStatsAdapter = QuickStatsAdapter(quickStats)
        deviceBinding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        deviceBinding.horizontalRecyclerView.adapter = quickStatsAdapter
    }

    private fun setupFabExport() {
        deviceBinding.fabExport.setOnClickListener {
            exportToFile()
        }
    }

    private fun setupFilterChip() {
        deviceBinding.filterChip.setOnClickListener {
            showFilterDialog()
        }
    }

    // ====== Menu Functions ======

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_device_info, menu)

        // Setup search view
        val searchItem = menu.findItem(R.id.action_share)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterDeviceInfo(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterDeviceInfo(newText)
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_copy_all -> {
                copyAllToClipboard()
                true
            }
            R.id.action_export -> {
                exportToFile()
                true
            }
            R.id.action_share -> {
                shareData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_device_info, popupMenu.menu)
        // Apply style programmatically
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val mPopup = fieldPopup.get(popupMenu)

            // Show icons
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)

            // Customize other properties
            mPopup.javaClass.getDeclaredMethod("setModal", Boolean::class.java)
                .invoke(mPopup, false)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_copy_all -> {
                    copyAllToClipboard()
                    true
                }
                R.id.action_export -> {
                    exportToFile()
                    true
                }
                R.id.action_share -> {
                    shareData()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showSettingsDialog() {
        // Implement your settings dialog here
        Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show()
    }

    // ====== Main Info Display Function ======

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showInfo(category: String) {
        val data: List<DeviceInfoItem> = when (category) {
            "Network Type" -> listOf(DeviceInfoItem("Network Type", getNetworkType()))
            "Battery Info" -> getBatteryInfo()
            "Get Locale" -> getLocaleInfo()
            "Screen Info" -> getScreenInfo()
            "Get App Info" -> getAppInfo()
            "Get Build and OS Info" -> getDeviceInfo()
            "Memory & Storage" -> getMemoryAndStorageInfo()
            "CPU & Hardware" -> getCpuAndHardwareInfo()
            "Sensors" -> getSensorsInfo()
            "Camera Info" -> getCameraInfo()
            "Network Details" -> getNetworkDetails()
            else -> listOf(DeviceInfoItem("Error", "Unknown selection"))
        }

        deviceInfoAdapter?.updateData(data)

        // Update UI
        deviceBinding.allInfoLabel.text = "$category (${data.size} items)"
    }

    // ====== Info Methods ======

    private fun getNetworkType(): String {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(network)
            when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
                else -> "Unknown"
            }
        } else {
            val network = cm.activeNetworkInfo
            network?.typeName ?: "No connection"
        }
    }

    private fun getBatteryInfo(): List<DeviceInfoItem> {
        val intent = requireContext().registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPercent = (level * 100 / scale.toFloat()).toInt()

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plug = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargePlug = when (plug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not charging"
        }

        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val batteryHealth = when (health) {
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }

        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempCelsius = temperature / 10f

        return listOf(
            DeviceInfoItem("Battery Level", "$batteryPercent%"),
            DeviceInfoItem("Charging Status", if (isCharging) "Charging" else "Not Charging"),
            DeviceInfoItem("Charge Source", chargePlug),
            DeviceInfoItem("Battery Health", batteryHealth),
            DeviceInfoItem("Temperature", "${tempCelsius}°C"),
            DeviceInfoItem("Technology", intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"),
            DeviceInfoItem("Voltage", "${intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1} mV")
        )
    }

    private fun getLocaleInfo(): List<DeviceInfoItem> {
        val locale = Locale.getDefault()
        return listOf(
            DeviceInfoItem("Language", locale.language),
            DeviceInfoItem("Country", locale.country),
            DeviceInfoItem("Display Language", locale.displayLanguage),
            DeviceInfoItem("Display Country", locale.displayCountry),
            DeviceInfoItem("ISO3 Language", locale.isO3Language),
            DeviceInfoItem("ISO3 Country", locale.isO3Country),
            DeviceInfoItem("Display Name", locale.displayName),
            DeviceInfoItem("Script", locale.script),
            DeviceInfoItem("Variant", locale.variant)
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getScreenInfo(): List<DeviceInfoItem> {
        val display = requireContext().display
        val metrics = Resources.getSystem().displayMetrics
        val widthPixels = metrics.widthPixels
        val heightPixels = metrics.heightPixels
        val density = metrics.density
        val densityDpi = metrics.densityDpi
        val scaledDensity = metrics.scaledDensity
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi

        return listOf(
            DeviceInfoItem("Resolution", "${widthPixels}x${heightPixels}"),
            DeviceInfoItem("Density", String.format("%.2f", density)),
            DeviceInfoItem("Density DPI", "$densityDpi dpi"),
            DeviceInfoItem("Scaled Density", String.format("%.2f", scaledDensity)),
            DeviceInfoItem("XDPI", String.format("%.2f", xdpi)),
            DeviceInfoItem("YDPI", String.format("%.2f", ydpi)),
            DeviceInfoItem("Refresh Rate", "${display?.refreshRate ?: 60} Hz"),
            DeviceInfoItem("Physical Size", getScreenSizeInInches())
        )
    }

    private fun getScreenSizeInInches(): String {
        val metrics = Resources.getSystem().displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches =
            sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
        return String.format("%.1f\"", diagonalInches)
    }

    private fun getAppInfo(): List<DeviceInfoItem> {
        val pm = requireContext().packageManager
        val pkg = requireContext().packageName
        val info = pm.getPackageInfo(pkg, 0)
        val appName = requireContext().applicationInfo.loadLabel(pm).toString()

        val installTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(info.firstInstallTime))
        val updateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(info.lastUpdateTime))

        return listOf(
            DeviceInfoItem("App Name", appName),
            DeviceInfoItem("Package Name", pkg),
            DeviceInfoItem("Version Name", info.versionName?:"NA"),
            DeviceInfoItem("Version Code", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode.toString()
            } else {
                info.versionCode.toString()
            }),
            DeviceInfoItem("Install Time", installTime),
            DeviceInfoItem("Update Time", updateTime),
            DeviceInfoItem("Target SDK", "${info.applicationInfo?.targetSdkVersion?:"NA"}"),
            DeviceInfoItem("Min SDK", "${info.applicationInfo?.minSdkVersion?:"NA"}"),
            DeviceInfoItem("Process Name", info.applicationInfo?.processName?:"NA")
        )
    }

    private fun getDeviceInfo(): List<DeviceInfoItem> {
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(Build.TIME))

        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "Not available"
        }

        return listOf(
            DeviceInfoItem("Android ID", getAndroidId(requireContext())),
            DeviceInfoItem("Model", Build.MODEL),
            DeviceInfoItem("Manufacturer", Build.MANUFACTURER),
            DeviceInfoItem("Brand", Build.BRAND),
            DeviceInfoItem("Device", Build.DEVICE),
            DeviceInfoItem("Product", Build.PRODUCT),
            DeviceInfoItem("Board", Build.BOARD),
            DeviceInfoItem("Hardware", Build.HARDWARE),
            DeviceInfoItem("OS Version", Build.VERSION.RELEASE),
            DeviceInfoItem("API Level", Build.VERSION.SDK_INT.toString()),
            DeviceInfoItem("Build ID", Build.ID),
            DeviceInfoItem("Build Time", buildTime),
            DeviceInfoItem("Fingerprint", Build.FINGERPRINT),
            DeviceInfoItem("Security Patch", securityPatch),
            DeviceInfoItem("Bootloader", Build.BOOTLOADER),
            DeviceInfoItem("Radio Version", Build.getRadioVersion()),
            DeviceInfoItem("Tags", Build.TAGS),
            DeviceInfoItem("Type", Build.TYPE),
            DeviceInfoItem("User", Build.USER),
            DeviceInfoItem("Host", Build.HOST)
        )
    }

    private fun getMemoryAndStorageInfo(): List<DeviceInfoItem> {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        val usedRam = totalRam - availableRam
        val ramUsagePercent = (usedRam.toFloat() / totalRam.toFloat() * 100).toInt()

        val statFs = StatFs(Environment.getDataDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong

        val totalStorage = totalBlocks * blockSize
        val availableStorage = availableBlocks * blockSize
        val usedStorage = totalStorage - availableStorage
        val storageUsagePercent = (usedStorage.toFloat() / totalStorage.toFloat() * 100).toInt()

        return listOf(
            DeviceInfoItem("Total RAM", formatBytes(totalRam)),
            DeviceInfoItem("Available RAM", formatBytes(availableRam)),
            DeviceInfoItem("Used RAM", formatBytes(usedRam)),
            DeviceInfoItem("RAM Usage", "$ramUsagePercent%"),
            DeviceInfoItem("Low Memory", if (memoryInfo.lowMemory) "Yes" else "No"),
            DeviceInfoItem("Threshold", formatBytes(memoryInfo.threshold)),
            DeviceInfoItem("Total Storage", formatBytes(totalStorage)),
            DeviceInfoItem("Available Storage", formatBytes(availableStorage)),
            DeviceInfoItem("Used Storage", formatBytes(usedStorage)),
            DeviceInfoItem("Storage Usage", "$storageUsagePercent%"),
            DeviceInfoItem("Is External Storage", if (Environment.isExternalStorageRemovable()) "Yes" else "No"),
            DeviceInfoItem("External Storage State", Environment.getExternalStorageState())
        )
    }

    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format("%.2f %s", size, units[unitIndex])
    }

    private fun getCpuAndHardwareInfo(): List<DeviceInfoItem> {
        val processorCount = Runtime.getRuntime().availableProcessors()

        val abis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Build.SUPPORTED_ABIS.joinToString(", ")
        } else {
            Build.CPU_ABI
        }

        val cpuInfo = try {
            val cpuInfoFile = File("/proc/cpuinfo")
            if (cpuInfoFile.exists()) {
                cpuInfoFile.readText().split("\n")
                    .filter { it.contains("model name") || it.contains("Processor") }
                    .firstOrNull()?.split(":")?.getOrNull(1)?.trim() ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }

        return listOf(
            DeviceInfoItem("Processor Cores", processorCount.toString()),
            DeviceInfoItem("CPU Info", cpuInfo),
            DeviceInfoItem("CPU Architecture", Build.CPU_ABI),
            DeviceInfoItem("Supported ABIs", abis),
            DeviceInfoItem("Hardware", Build.HARDWARE),
            DeviceInfoItem("Device", Build.DEVICE),
            DeviceInfoItem("Board", Build.BOARD),
            DeviceInfoItem("Product", Build.PRODUCT)
        )
    }

    private fun getSensorsInfo(): List<DeviceInfoItem> {
        val sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val sensorTypes = listOf(
            Pair("Accelerometer", Sensor.TYPE_ACCELEROMETER),
            Pair("Gyroscope", Sensor.TYPE_GYROSCOPE),
            Pair("Proximity", Sensor.TYPE_PROXIMITY),
            Pair("Light", Sensor.TYPE_LIGHT),
            Pair("Magnetic Field", Sensor.TYPE_MAGNETIC_FIELD),
            Pair("Pressure", Sensor.TYPE_PRESSURE),
            Pair("Humidity", Sensor.TYPE_RELATIVE_HUMIDITY),
            Pair("Ambient Temperature", Sensor.TYPE_AMBIENT_TEMPERATURE),
            Pair("Step Counter", Sensor.TYPE_STEP_COUNTER),
            Pair("Heart Rate", Sensor.TYPE_HEART_RATE)
        )

        val sensorList = mutableListOf<DeviceInfoItem>()
        sensorList.add(DeviceInfoItem("Total Sensors", sensors.size.toString()))

        sensorTypes.forEach { (name, type) ->
            val sensor = sensors.find { it.type == type }
            sensorList.add(DeviceInfoItem(name, if (sensor != null) "Available" else "Not Available"))
        }

        return sensorList
    }

    private fun getCameraInfo(): List<DeviceInfoItem> {
        return try {
            val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList

            val cameraList = mutableListOf<DeviceInfoItem>()
            cameraList.add(DeviceInfoItem("Total Cameras", cameraIds.size.toString()))

            cameraIds.forEachIndexed { index, cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                val facing = when (lensFacing) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    else -> "External"
                }
                cameraList.add(DeviceInfoItem("Camera $index", facing))
            }

            cameraList
        } catch (e: Exception) {
            listOf(DeviceInfoItem("Camera Info", "Permission required or not available"))
        }
    }

    private fun getNetworkDetails(): List<DeviceInfoItem> {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetworkInfo

        val wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiInfo = wifiManager?.connectionInfo

        val networkList = mutableListOf<DeviceInfoItem>()

        networkList.add(DeviceInfoItem("Network Type", getNetworkType()))
        networkList.add(DeviceInfoItem("Is Connected", if (network?.isConnected == true) "Yes" else "No"))
        networkList.add(DeviceInfoItem("Is Roaming", if (network?.isRoaming == true) "Yes" else "No"))
        networkList.add(DeviceInfoItem("Is Failover", if (network?.isFailover == true) "Yes" else "No"))
        networkList.add(DeviceInfoItem("Reason", network?.reason ?: "Unknown"))
        networkList.add(DeviceInfoItem("Extra Info", network?.extraInfo ?: "None"))

        if (wifiInfo != null) {
            networkList.add(DeviceInfoItem("WiFi SSID", wifiInfo.ssid.replace("\"", "")))
            networkList.add(DeviceInfoItem("WiFi BSSID", wifiInfo.bssid))
            networkList.add(DeviceInfoItem("WiFi Signal", "${wifiInfo.rssi} dBm"))
            networkList.add(DeviceInfoItem("Link Speed", "${wifiInfo.linkSpeed} Mbps"))
            networkList.add(DeviceInfoItem("Frequency", "${wifiInfo.frequency} MHz"))
            networkList.add(DeviceInfoItem("IP Address", wifiInfo.ipAddress.toString()))
            networkList.add(DeviceInfoItem("MAC Address", wifiInfo.macAddress))
        }

        networkList.add(DeviceInfoItem("Local IP", getLocalIPAddress()))

        return networkList
    }

    // ====== Utility Functions ======

    private fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "UNKNOWN_ANDROID_ID"
        } catch (e: Exception) {
            Log.e("DeviceFragment", "Error getting Android ID: ${e.message}")
            "UNKNOWN_ANDROID_ID"
        }
    }

    private fun getLocalIPAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
            "No IP found"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun getCurrentRamUsage(): String {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val used = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
        val total = memoryInfo.totalMem / (1024 * 1024)
        val percentage = (used.toFloat() / total.toFloat() * 100).toInt()

        return "$percentage%"
    }

    private fun getStorageUsage(): String {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val total = statFs.totalBytes
        val free = statFs.availableBytes
        val used = total - free
        val percentage = (used.toFloat() / total.toFloat() * 100).toInt()

        return "$percentage%"
    }

    private fun getBatteryPercentage(): String {
        val batteryIntent = requireContext().registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        return "${(level * 100 / scale)}%"
    }

    private fun toggleTheme() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val newNightMode = when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
            Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(newNightMode)
        requireActivity().recreate()
    }

    private fun updateThemeIcon() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val iconRes = when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> R.drawable.ic_light_mode
            Configuration.UI_MODE_NIGHT_NO -> R.drawable.ic_dark_mode
            else -> R.drawable.ic_dark_mode
        }
        deviceBinding.themeToggleButton.setIconResource(iconRes)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun refreshData() {
        showInfo(currentCategory)
        Toast.makeText(requireContext(), "Data refreshed", Toast.LENGTH_SHORT).show()
    }

    private fun filterDeviceInfo(query: String) {
        deviceInfoAdapter?.filter?.filter(query)
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, "$label: $value")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied: $label", Toast.LENGTH_SHORT).show()
    }

    private fun copyAllToClipboard() {
        val allText = deviceInfoAdapter?.getAllData()?.joinToString("\n") { "${it.label}: ${it.value}" }
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Device Info", allText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "All data copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun exportToFile() {
        val allData = deviceInfoAdapter?.getAllData()
        val content = StringBuilder()

        content.append("Device Information Report\n")
        content.append("===================================\n")
        content.append("Category: $currentCategory\n")
        content.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        content.append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
        content.append("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
        content.append("===================================\n\n")

        allData?.forEach { item ->
            content.append("${item.label}: ${item.value}\n")
        }

        // Save to file
        val fileName = "device_info_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.txt"
        val file = File(requireContext().getExternalFilesDir(null), fileName)
        file.writeText(content.toString())

        // Share file
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Device Information Report")
            putExtra(Intent.EXTRA_TEXT, content.toString())
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Device Info"))
        Toast.makeText(requireContext(), "Exported to file", Toast.LENGTH_SHORT).show()
    }

    private fun shareData() {
        val allData = deviceInfoAdapter?.getAllData()
        val content = StringBuilder()

        content.append("Device Information:\n")
        content.append("==================\n")
        allData?.forEach { item ->
            content.append("${item.label}: ${item.value}\n")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content.toString())
        }

        startActivity(Intent.createChooser(shareIntent, "Share Device Info"))
    }

    private fun showFilterDialog() {
        // Implement filter dialog based on your needs
        Toast.makeText(requireContext(), "Filter functionality to be implemented", Toast.LENGTH_SHORT).show()
    }
}