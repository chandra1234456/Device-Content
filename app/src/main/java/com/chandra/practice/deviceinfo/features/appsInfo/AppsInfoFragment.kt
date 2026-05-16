package com.chandra.practice.deviceinfo.features.appsInfo


import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.databinding.FragmentAppsInfoBinding
import java.io.File

class AppsInfoFragment : Fragment() {

    private var _binding: FragmentAppsInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var allApps: List<AppInfo>

    private val appsAdapter: AppsAdapter by lazy {
        AppsAdapter(emptyList())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        loadAppsData()
        setupToggle()
    }

    private fun setupRecycler() {
        binding.rvInstalledApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appsAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadAppsData() {

        val context = requireContext()
        val pm = context.packageManager

        val apps = pm.getInstalledApplications(0)
            .filter {
                pm.getLaunchIntentForPackage(it.packageName) != null
            }

        val total = apps.size

        val system = apps.count {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }

        val user = total - system

        binding.tvTotalCount.text = total.toString()
        binding.tvSystemCount.text = system.toString()
        binding.tvUserCount.text = user.toString()

        allApps = apps.map { it.toAppInfo(context) }

        // Default = System apps
        showSystemApps()
    }

    private fun setupToggle() {

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->

            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {

                R.id.btnSystem -> {
                    showSystemApps()
                }

                R.id.btnUser -> {
                    showUserApps()
                }
            }
        }
    }

    private fun showSystemApps() {
        appsAdapter.updateList(
            allApps.filter { it.isSystemApp }
        )
    }

    private fun showUserApps() {
        appsAdapter.updateList(
            allApps.filter { !it.isSystemApp }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun ApplicationInfo.toAppInfo(context: Context): AppInfo {

        val pm = context.packageManager
        val packageInfo = pm.getPackageInfo(packageName, 0)

        return AppInfo(
            name = pm.getApplicationLabel(this).toString(),
            packageName = packageName,
            icon = pm.getApplicationIcon(this),
            version = packageInfo.versionName ?: "N/A",
            size = File(sourceDir).length().toString(),
            isSystemApp = (flags and ApplicationInfo.FLAG_SYSTEM) != 0
        )
    }
}