package com.chandra.practice.deviceinfo.features.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.databinding.FragmentBatteryInfoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatteryInfoFragment : Fragment() {

    private var _binding: FragmentBatteryInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var batterySpecAdapter: BatterySpecAdapter
    private val specList = mutableListOf<SpecItem>()

    private var currentBatteryPercent = 0
    private var isCharging = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null && intent != null) {
                try {
                    loadBatteryInfo(context, intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle gracefully
                    showError("Failed to load battery info")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatteryInfoBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupButtons()
        return binding.root
    }

    private fun setupRecyclerView() {
        batterySpecAdapter = BatterySpecAdapter(specList)
        binding.recyclerSpecs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = batterySpecAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupButtons() {
        binding.btnExport.setOnClickListener {
            exportBatteryLog()
        }

        binding.btnRecalibrate.setOnClickListener {
            recalibrateBattery()
        }
    }

    private fun loadBatteryInfo(context: Context, intent: Intent) {
        if (_binding == null) return

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        // Battery Percentage
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        if (level == -1 || scale <= 0) return

        val batteryPercent = ((level * 100) / scale.toFloat()).toInt()
        currentBatteryPercent = batteryPercent

        // Charging Status
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val chargingStatus = getChargingStatusString(status)
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Battery Health
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val batteryHealth = getHealthString(health)

        // Charging Type
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargingType = getChargingTypeString(plugged)

        // Temperature (in Celsius) - This is safe to use
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0f

        // Voltage - This is safe to use
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0f

        // Technology - This is safe to use
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        // Power Save Mode - This is safe to use
        val powerSaveMode = if (powerManager.isPowerSaveMode) "Enabled" else "Disabled"

        // Battery Present - This is safe to use
        val isPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)

        // REMOVED: getIntProperty calls that require BATTERY_STATS permission
        // - Cycle count (requires permission)
        // - Capacity/Charge counter (requires permission)
        // - Current now (requires permission)

        // Get estimated capacity from intent if available (some devices)
        val maxChargingCurrent = 10// intent.getIntExtra(BatteryManager.EXTRA_MAX_CHARGING_CURRENT, -1)
        val maxChargingVoltage = 10//intent.getIntExtra(BatteryManager.EXTRA_MAX_CHARGING_VOLTAGE, -1)

        // Update UI elements
        updateBatteryUI(batteryPercent)
        updateTemperatureAndVoltage(temperature, voltage)
        updateTimeEstimation(batteryPercent, isCharging)

        // Update spec list without restricted properties
        updateSpecList(
            chargingStatus,
            chargingType,
            batteryHealth,
            technology,
            powerSaveMode,
        )
    }

    private fun updateBatteryUI(percentage: Int) {
        binding.tvPercentage.text = "$percentage%"
        binding.tvChargingStatus.text = when {
            isCharging && percentage == 100 -> "Fully Charged ✓"
            isCharging -> "Charging ⚡"
            else -> "Discharging"
        }

        // Update Circular Progress Indicator
        binding.circularBattery.setProgressCompat(percentage, true)

        // Change color based on percentage
        val color = when {
            percentage >= 75 -> ContextCompat.getColor(requireContext(), R.color.battery_green)
            percentage >= 50 -> ContextCompat.getColor(requireContext(), R.color.battery_blue)
            percentage >= 25 -> ContextCompat.getColor(requireContext(), R.color.battery_orange)
            else -> ContextCompat.getColor(requireContext(), R.color.battery_red)
        }
        binding.circularBattery.setIndicatorColor(color)

        // Change text color for percentage
        binding.tvPercentage.setTextColor(color)
    }

    private fun updateTemperatureAndVoltage(temperature: Float, voltage: Float) {
        if (temperature > 0) {
            binding.tvTemp.text = String.Companion.format(Locale.US, "%.1f°C", temperature)

            // Temperature warning
            when {
                temperature > 45 -> {
                    binding.tvTemp.setTextColor(ContextCompat.getColor(requireContext(), R.color.battery_red))
                    if (isResumed) {
                        Toast.makeText(requireContext(), "⚠️ Battery temperature is high!", Toast.LENGTH_SHORT).show()
                    }
                }
                temperature > 35 -> {
                    binding.tvTemp.setTextColor(ContextCompat.getColor(requireContext(), R.color.battery_orange))
                }
                else -> {
                    binding.tvTemp.setTextColor(ContextCompat.getColor(requireContext(), R.color.battery_green))
                }
            }
        } else {
            binding.tvTemp.text = "N/A"
        }

        if (voltage > 0) {
            binding.tvVoltage.text = String.Companion.format(Locale.US, "%.2f V", voltage)
        } else {
            binding.tvVoltage.text = "N/A"
        }
    }

    private fun updateTimeEstimation(percentage: Int, charging: Boolean) {
        val estimationText = if (charging) {
            if (percentage == 100) {
                "Battery fully charged"
            } else {
                // Rough estimation: ~1% per 2 minutes for charging
                val minutesToFull = (100 - percentage) * 2
                if (minutesToFull > 0) {
                    val hours = minutesToFull / 60
                    val minutes = minutesToFull % 60
                    if (hours > 0) {
                        "≈ ${hours}h ${minutes}m until full"
                    } else {
                        "≈ ${minutes}m until full"
                    }
                } else {
                    "Almost full"
                }
            }
        } else {
            // Rough estimation: ~1% per 3-5 minutes for discharging
            val minutesRemaining = percentage * 4
            val hours = minutesRemaining / 60
            val minutes = minutesRemaining % 60
            if (hours > 0) {
                "≈ ${hours}h ${minutes}m remaining"
            } else {
                "≈ ${minutes}m remaining"
            }
        }
        binding.tvEstimation.text = estimationText
    }

    private fun updateSpecList(
        chargingStatus: String,
        chargingType: String,
        batteryHealth: String,
        technology: String,
        powerSaveMode: String,
    ) {
        specList.clear()

        val items = mutableListOf(
            SpecItem(
                ContextCompat.getDrawable(context, R.drawable.ic_voltage),
                "Charging Status",
                chargingStatus
            ),
            SpecItem(
                ContextCompat.getDrawable(context, R.drawable.ic_health),
                "Battery Health",
                batteryHealth
            ),
            SpecItem(
                ContextCompat.getDrawable(context, R.drawable.ic_technology),
                "Technology",
                technology
            ),
            SpecItem(
                ContextCompat.getDrawable(context, R.drawable.ic_voltage),
                "Power Save Mode",
                powerSaveMode
            )
        )

        // Add charging info if available
        /*if (maxChargingCurrent > 0) {
            items.add(SpecItem("Max Charging Current", "$maxChargingCurrent mA"))
        }
        if (maxChargingVoltage > 0) {
            items.add(SpecItem("Max Charging Voltage", "$maxChargingVoltage mV"))
        }
*/
        // Add note about restricted permissions

        specList.addAll(items)
        batterySpecAdapter.notifyDataSetChanged()
    }

    private fun getChargingStatusString(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }

    private fun getHealthString(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good ✓"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat ⚠️"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead ✗"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage ⚠️"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold ❄️"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure ✗"
            else -> "Unknown"
        }
    }

    private fun getChargingTypeString(plugged: Int): String {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not Plugged"
        }
    }

    private fun exportBatteryLog() {
        try {
            val timestamp = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            val logData = buildString {
                appendLine("=== Battery Information Export ===")
                appendLine("Timestamp: $timestamp")
                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                appendLine("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine()
                appendLine("=== Battery Details ===")
                specList.forEach { spec ->
                    if (spec.title != "Note") { // Skip the note in export
                        appendLine("${spec.title}: ${spec.value}")
                    }
                }
                appendLine()
                appendLine("=== Health Tips ===")
                appendLine("• Keep battery between 20% - 80% for longevity")
                appendLine("• Avoid extreme temperatures")
                appendLine("• Use original chargers when possible")
            }

            // Share the log
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, logData)
                putExtra(Intent.EXTRA_SUBJECT, "Battery Report - $timestamp")
                setType("text/plain")
            }

            startActivity(Intent.createChooser(shareIntent, "Export Battery Log"))
            Toast.makeText(requireContext(), "Battery log ready to share", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            showError("Failed to export log: ${e.message}")
        }
    }

    private fun recalibrateBattery() {
        try {
            AlertDialog.Builder(requireContext())
                .setTitle("Battery Recalibration")
                .setMessage(
                    "To help improve battery accuracy:\n\n" +
                            "1️⃣ Use device until it completely drains and turns off\n" +
                            "2️⃣ Charge to 100% without interruption\n" +
                            "3️⃣ Keep charging for 1 extra hour after reaching 100%\n" +
                            "4️⃣ Restart your device\n\n" +
                            "⚠️ Note: Perform this every 2-3 months for best results"
                )
                .setPositiveButton("Got it") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Follow the steps to recalibrate", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            showError("Failed to show recalibration guide")
        }
    }

    private fun showError(message: String) {
        if (isResumed) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            requireContext().registerReceiver(
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to register battery monitor")
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(batteryReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver wasn't registered
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}