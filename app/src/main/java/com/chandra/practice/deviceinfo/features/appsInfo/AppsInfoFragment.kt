package com.chandra.practice.deviceinfo.features.appsInfo

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.databinding.FragmentAppsInfoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppsInfoFragment : Fragment() {

    private var _binding: FragmentAppsInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var allApps: List<AppInfo>

    private val appsAdapter: AppsAdapter by lazy {
        AppsAdapter(emptyList()) { appInfo ->
            showAppDetailsBottomSheet(appInfo)
        }
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
        setupToggle()
        setupSearch()
        setupSort()
        setupBackNavigation()
        setupStorageAlertButton()
        loadAppsData()
    }

    private fun setupRecycler() {
        binding.rvInstalledApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupToggle() {
        binding.toggleGroup.addOnButtonCheckedListener { _, _, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            filterAndDisplayApps()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAndDisplayApps()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSort() {
        binding.chipSortName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) filterAndDisplayApps()
        }
        binding.chipSortSize.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) filterAndDisplayApps()
        }
        binding.chipSortRecent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) filterAndDisplayApps()
        }
    }

    private fun setupBackNavigation() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupStorageAlertButton() {
        binding.btnAnalyze.setOnClickListener {
            binding.toggleGroup.check(R.id.btnUser)
            binding.chipGroupSort.check(R.id.chipSortSize)
            // Scroll to the list
            binding.cardAppsList.parent.requestChildFocus(binding.cardAppsList, binding.cardAppsList)
        }
    }

    private fun loadAppsData() {
        try {
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

            binding.progressSystem.max = total
            binding.progressSystem.progress = system

            binding.progressUser.max = total
            binding.progressUser.progress = user

            allApps = apps.map { it.toAppInfo(context) }

            // Setup storage alert (User apps larger than 100MB)
            val largeAppsCount = allApps.count { !it.isSystemApp && it.sizeBytes > 100 * 1024 * 1024 }
            if (largeAppsCount > 0) {
                binding.cardStorageAlert.visibility = View.VISIBLE
                binding.tvStorageAlertDesc.text = "$largeAppsCount apps are using more than 100MB of space. Consider clearing data to free up space."
            } else {
                binding.cardStorageAlert.visibility = View.GONE
            }

            // Default = System apps
            binding.toggleGroup.check(R.id.btnSystem)
            filterAndDisplayApps()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to load apps data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterAndDisplayApps() {
        if (!::allApps.isInitialized) return

        val isSystem = binding.toggleGroup.checkedButtonId == R.id.btnSystem
        val query = binding.etSearch.text.toString().trim()

        // Filter system vs user
        var filteredList = allApps.filter { it.isSystemApp == isSystem }

        // Filter search query
        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }

        // Sort list
        filteredList = when (binding.chipGroupSort.checkedChipId) {
            R.id.chipSortSize -> filteredList.sortedByDescending { it.sizeBytes }
            R.id.chipSortRecent -> filteredList.sortedByDescending { it.updateTime }
            else -> filteredList.sortedBy { it.name.lowercase() }
        }

        appsAdapter.updateList(filteredList)
        binding.tvAppCountLabel.text = "Showing ${filteredList.size} apps"
    }

    private fun showAppDetailsBottomSheet(app: AppInfo) {
        try {
            val context = requireContext()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val installDate = dateFormat.format(Date(app.installTime))
            val updateDate = dateFormat.format(Date(app.updateTime))

            val message = buildString {
                append("Package: ${app.packageName}\n\n")
                append("Version: ${app.version}\n")
                append("Size: ${AppsAdapter.formatSize(app.sizeBytes)}\n")
                append("Min SDK: API ${app.minSdk}\n")
                append("Target SDK: API ${app.targetSdk}\n\n")
                append("Installed: $installDate\n")
                append("Last Updated: $updateDate")
            }

            MaterialAlertDialogBuilder(context)
                .setIcon(app.icon)
                .setTitle(app.name)
                .setMessage(message)
                .setPositiveButton("App Settings") { dialog, _ ->
                    dialog.dismiss()
                    openAppSettings(app.packageName)
                }
                .setNeutralButton("Launch App") { dialog, _ ->
                    dialog.dismiss()
                    launchApp(app.packageName)
                }
                .setNegativeButton("Close", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppSettings(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "App cannot be launched directly", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to launch app", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun ApplicationInfo.toAppInfo(context: Context): AppInfo {
        val pm = context.packageManager
        val packageInfo = pm.getPackageInfo(packageName, 0)
        val sizeBytes = File(sourceDir).length()

        return AppInfo(
            name = pm.getApplicationLabel(this).toString(),
            packageName = packageName,
            icon = pm.getApplicationIcon(this),
            version = packageInfo.versionName ?: "N/A",
            size = sizeBytes.toString(),
            sizeBytes = sizeBytes,
            updateTime = packageInfo.lastUpdateTime,
            installTime = packageInfo.firstInstallTime,
            targetSdk = targetSdkVersion,
            minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) minSdkVersion else 21,
            isSystemApp = (flags and ApplicationInfo.FLAG_SYSTEM) != 0
        )
    }
}