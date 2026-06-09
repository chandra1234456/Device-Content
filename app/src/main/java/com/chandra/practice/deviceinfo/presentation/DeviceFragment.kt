package com.chandra.practice.deviceinfo.presentation

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.adapter.CategoryAdapter
import com.chandra.practice.deviceinfo.data.CategoryItem
import com.chandra.practice.deviceinfo.databinding.FragmentDeviceBinding
import java.util.Locale

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val deviceBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return deviceBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupQuickStats()
        setupCategoriesGrid()
        setupClickListeners()
        playEntranceAnimations()
    }

    private fun setupQuickStats() {
        val context = requireContext()
        
        // 1. Device Manufacturer & Model
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
        val model = Build.MODEL
        deviceBinding.tvDeviceName.text = "$manufacturer $model"
        
        // 2. Android version & SDK API Level
        deviceBinding.tvDeviceOs.text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        // 3. Real-time Battery Percentage & Charge status
        try {
            val batteryStatus: Intent? = context.registerReceiver(
                null, 
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 0
            
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                              status == BatteryManager.BATTERY_STATUS_FULL
            
            val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val plugType = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Bat"
            }
            
            val batteryText = if (isCharging) "$batteryPct% • $plugType ⚡" else "$batteryPct% • Discharging"
            deviceBinding.tvBatteryValue.text = batteryText

            // Dynamic battery icon tint based on charge level
            val batteryColor = when {
                batteryPct >= 70 -> ContextCompat.getColor(context, R.color.battery_green)
                batteryPct >= 35 -> ContextCompat.getColor(context, R.color.battery_orange)
                else -> ContextCompat.getColor(context, R.color.battery_red)
            }
            deviceBinding.ivBatteryStat.setColorFilter(batteryColor)
        } catch (e: Exception) {
            deviceBinding.tvBatteryValue.text = "N/A"
            e.printStackTrace()
        }

        // 4. Memory/RAM Usage
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val availRamGb = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)
            val usedRamGb = totalRamGb - availRamGb
            
            deviceBinding.tvRamValue.text = String.format(Locale.US, "%.1f/%.1f GB", usedRamGb, totalRamGb)
        } catch (e: Exception) {
            deviceBinding.tvRamValue.text = "N/A"
            e.printStackTrace()
        }

        // 5. Disk/Storage Space
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            
            val totalStorageGb = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            val availStorageGb = (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            val usedStorageGb = totalStorageGb - availStorageGb
            
            deviceBinding.tvStorageValue.text = String.format(Locale.US, "%.1f/%.1f GB", usedStorageGb, totalStorageGb)
        } catch (e: Exception) {
            deviceBinding.tvStorageValue.text = "N/A"
            e.printStackTrace()
        }
    }

    private fun setupCategoriesGrid() {
        val categories = listOf(
            CategoryItem("Network Type", R.drawable.ic_wifi, R.color.category_network),
            CategoryItem("Battery Info", R.drawable.ic_battery_info, R.color.category_battery),
            CategoryItem("Locale", R.drawable.ic_locale, R.color.category_locale),
            CategoryItem("Screen Info", R.drawable.ic_screen, R.color.category_screen),
            CategoryItem("Apps Info", R.drawable.ic_apps, R.color.category_apps),
            CategoryItem("Build & OS Info", R.drawable.ic_android, R.color.category_build),
            CategoryItem("Memory & Storage", R.drawable.ic_storage, R.color.category_memory),
            CategoryItem("CPU & Hardware", R.drawable.ic_cpu, R.color.category_cpu),
            CategoryItem("Sensors", R.drawable.ic_sensors_type, R.color.category_sensors),
            CategoryItem("Camera Info", R.drawable.ic_camera_info, R.color.category_camera),
            CategoryItem("Network Details", R.drawable.ic_network_details, R.color.category_details)
        )

        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val itemCount = categories.size
                return if (itemCount % 2 != 0 && position == itemCount - 1) {
                    2 // Last item takes the full span width to fill the row
                } else {
                    1 // Other grid items occupy 1/2 of the span width
                }
            }
        }

        deviceBinding.recyclerView.layoutManager = layoutManager
        deviceBinding.recyclerView.adapter = CategoryAdapter(categories) { category ->
            when (category) {
                "Battery Info" -> {
                    findNavController().navigate(R.id.batteryInfoFragment)
                }
                "Network Details" -> {
                    findNavController().navigate(R.id.networkDetailsFragment)
                }
                "Apps Info" -> {
                    findNavController().navigate(R.id.appsInfoFragment)
                }
                else -> {
                    Toast.makeText(requireContext(), "$category details screen coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        deviceBinding.ivSettings.setOnClickListener {
            // Apply scale animation on settings click
            it.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .withEndAction {
                            findNavController().navigate(R.id.settingsFragment)
                        }
                        .start()
                }
                .start()
        }
    }

    private fun playEntranceAnimations() {
        // Slide up and fade in the Overview status card
        deviceBinding.cardOverview.alpha = 0f
        deviceBinding.cardOverview.translationY = 50f
        deviceBinding.cardOverview.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(100)
            .start()

        // Slide up and fade in the Category grid
        deviceBinding.recyclerView.alpha = 0f
        deviceBinding.recyclerView.translationY = 75f
        deviceBinding.recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
