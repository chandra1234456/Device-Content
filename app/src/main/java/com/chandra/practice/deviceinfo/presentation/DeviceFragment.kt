package com.chandra.practice.deviceinfo.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.adapter.CategoryAdapter
import com.chandra.practice.deviceinfo.data.CategoryItem
import com.chandra.practice.deviceinfo.databinding.FragmentDeviceBinding

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
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val categories = listOf(
            CategoryItem("Network Type", R.drawable.ic_wifi),
            CategoryItem("Battery Info", R.drawable.ic_battery_info),
            CategoryItem("Locale", R.drawable.ic_locale),
            CategoryItem("Screen Info", R.drawable.ic_screen),
            CategoryItem("Apps Info", R.drawable.ic_apps),
            CategoryItem("Build & OS Info", R.drawable.ic_app_info),
            CategoryItem("Memory & Storage", R.drawable.ic_storage_info),
            CategoryItem("CPU & Hardware", R.drawable.ic_core),
            CategoryItem("Sensors", R.drawable.ic_sensors_type),
            CategoryItem("Camera Info", R.drawable.ic_camera_info),
            CategoryItem("Network Details", R.drawable.ic_network)
        )
        val layoutManager = GridLayoutManager(requireContext(), 2)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val itemCount = categories.size

                return if (itemCount % 2 != 0 && position == itemCount - 1) {
                    2 // last item takes full width
                } else {
                    1 // normal items
                }
            }
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = CategoryAdapter(categories) { category ->
           Toast.makeText(requireContext(), category, Toast.LENGTH_SHORT).show()
           when(category){
               "Battery Info"->{
                   findNavController().navigate(R.id.batteryInfoFragment)
               }
               "Network Details"->{
                   findNavController().navigate(R.id.networkDetailsFragment)
               }
               "Apps Info"->{
                   findNavController().navigate(R.id.appsInfoFragment)
               }
           }
        }
        deviceBinding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
