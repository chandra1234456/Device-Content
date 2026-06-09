package com.chandra.practice.deviceinfo.features.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.databinding.FragmentNetworkDetailsBinding
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

class NetworkDetailsFragment : Fragment() {

    private var _binding: FragmentNetworkDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkDetailsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackNavigation()
        updateNetworkUI()
    }

    private fun setupBackNavigation() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    private fun updateNetworkUI() {
        try {
            val context = requireContext()

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager

            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE)
                        as WifiManager

            val network = connectivityManager.activeNetwork
            val capabilities =
                connectivityManager.getNetworkCapabilities(network)

            val isConnected = capabilities != null

            // STATUS
            binding.tvStatusValue.text =
                if (isConnected) "Connected" else "Disconnected"

            // CONNECTION TYPE
            val connectionType = when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ->
                    "Wi-Fi"

                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ->
                    "Mobile Data"

                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true ->
                    "Ethernet"

                else -> "Unknown"
            }

            binding.tvConnectionType.text = connectionType

            if (connectionType == "Wi-Fi") {
                val wifiInfo = wifiManager.connectionInfo

                // SSID
                binding.tvNetworkName.text =
                    wifiInfo.ssid?.replace("\"", "") ?: "Unknown"

                // Signal strength
                binding.tvSignalDbm.text =
                    "${wifiInfo.rssi} dBm"

                // Frequency
                binding.tvFrequencyValue.text =
                    if (wifiInfo.frequency >= 5000)
                        "5 GHz"
                    else
                        "2.4 GHz"

                // IP Address
                binding.tvIpAddress.text = intToIpAddress(wifiInfo.ipAddress)

                // MAC Address (may return randomized on Android 10+)
                binding.tvMacAddress.text =
                    wifiInfo.macAddress ?: "Unavailable"

                // DHCP info (Gateway + Subnet)
                val dhcpInfo = wifiManager.dhcpInfo

                binding.tvGateWay.text = intToIpAddress(dhcpInfo.gateway)
                binding.tvSubnetMask.text = intToIpAddress(dhcpInfo.netmask)

            } else {
                binding.tvNetworkName.text = "Mobile Network"
                binding.tvSignalDbm.text = "--"
                binding.tvFrequencyValue.text = "--"
                binding.tvIpAddress.text = getMobileIpAddress()
                binding.tvMacAddress.text = "Unavailable"
                binding.tvGateWay.text = "--"
                binding.tvSubnetMask.text = "--"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun intToIpAddress(ip: Int): String {
        return String.format(
            Locale.US,
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }

    private fun getMobileIpAddress(): String {
        return try {
            val interfaces =
                NetworkInterface.getNetworkInterfaces()

            for (networkInterface in interfaces) {
                val addresses =
                    networkInterface.inetAddresses

                for (address in addresses) {
                    if (!address.isLoopbackAddress &&
                        address is Inet4Address
                    ) {
                        return address.hostAddress ?: "--"
                    }
                }
            }
            "--"
        } catch (e: Exception) {
            "--"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}