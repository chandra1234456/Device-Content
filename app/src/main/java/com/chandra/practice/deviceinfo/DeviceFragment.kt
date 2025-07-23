package com.chandra.practice.deviceinfo

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.practice.deviceinfo.databinding.FragmentDeviceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DeviceFragment : Fragment() {

    private lateinit var deviceBinding: FragmentDeviceBinding
    private lateinit var adapter: HorizontalButtonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View {
        deviceBinding = FragmentDeviceBinding.inflate(layoutInflater)
        return deviceBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val infoOptions = listOf(
                "Network Type",
                "Battery Info",
                "Get Locale",
                "Screen Info",
                //"Get App Info",
                "Get Build and OS Info"
                                )

        adapter = HorizontalButtonAdapter(infoOptions) { clickedItem ->
            showInfo(clickedItem)
        }
        // 👉 Load default info on start
        val defaultCategory = infoOptions.first()  // or choose manually like "Battery Info"
        showInfo(defaultCategory)

        deviceBinding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        deviceBinding.horizontalRecyclerView.adapter = adapter
    }

    private fun showInfo(category: String) {
        val data: List<DeviceInfoItem> = when (category) {
            "Network Type" -> listOf(DeviceInfoItem("Network Type", getNetworkType()))
            "Battery Info" -> getBatteryInfo()
            "Get Locale" -> getLocaleInfo()
            "Screen Info" -> getScreenInfo()
            "Get App Info" -> getAppInfo()
            "Get Build and OS Info" -> getDeviceInfo()
            else -> listOf(DeviceInfoItem("Error", "Unknown selection"))
        }

        deviceBinding.deviceInfoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        deviceBinding.deviceInfoRecyclerView.adapter = DeviceInfoAdapter(data)
    }

    // ====== Info Methods (returning List<DeviceInfoItem>) ======

    private fun getNetworkType(): String {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetworkInfo
        return network?.typeName ?: "No connection"
    }

    private fun getBatteryInfo(): List<DeviceInfoItem> {
        val intent = requireContext().registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val status = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0
        val charging = if (status != 0) "Yes" else "No"

        return listOf(
                DeviceInfoItem("Battery Level", "$level%"),
                DeviceInfoItem("Charging", charging)
                     )
    }

    private fun getLocaleInfo(): List<DeviceInfoItem> {
        val locale = Locale.getDefault()
        return listOf(
                DeviceInfoItem("Language", locale.language),
                DeviceInfoItem("Country", locale.country)
                     )
    }

    private fun getScreenInfo(): List<DeviceInfoItem> {
        val metrics = Resources.getSystem().displayMetrics
        return listOf(
                DeviceInfoItem("Width", "${metrics.widthPixels}px"),
                DeviceInfoItem("Height", "${metrics.heightPixels}px"),
                DeviceInfoItem("Density", "${metrics.density}")
                     )
    }

    private fun getAppInfo(): List<DeviceInfoItem> {
        val pm = requireContext().packageManager
        val pkg = requireContext().packageName
        val info = pm.getPackageInfo(pkg, 0)
        val appName = requireContext().applicationInfo.loadLabel(pm).toString()

        return listOf(
                DeviceInfoItem("App Name", appName),
                DeviceInfoItem("Package", pkg),
                DeviceInfoItem("Version", info.versionName.toString())
                     )
    }

    private fun getDeviceInfo(): List<DeviceInfoItem> {
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(Build.TIME))

        return listOf(
                DeviceInfoItem("Android ID", getAndroidId(requireContext())),
                DeviceInfoItem("Model", Build.MODEL),
                DeviceInfoItem("Manufacturer", Build.MANUFACTURER),
                DeviceInfoItem("Brand", Build.BRAND),
                DeviceInfoItem("OS Version", Build.VERSION.RELEASE),
                DeviceInfoItem("SDK Version", Build.VERSION.SDK_INT.toString()),
                DeviceInfoItem("Build ID", Build.ID),
                DeviceInfoItem("Build Time", buildTime),
                DeviceInfoItem("Fingerprint", Build.FINGERPRINT)
                     )
    }

    private fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "UNKNOWN_ANDROID_ID"
        } catch (e: Exception) {
            Log.e("DeviceUtils", "Error getting Android ID: ${e.message}")
            "UNKNOWN_ANDROID_ID"
        }
    }
}
