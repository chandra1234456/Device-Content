package com.chandra.practice.deviceinfo.presentation

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.databinding.FragmentSettingsBinding
import com.chandra.practice.deviceinfo.databinding.ItemRowBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setUpOnClickEventListeners()
    }

    private fun setUpOnClickEventListeners() {
        binding.engagementLegal.ivEndIcon.setOnClickListener {
            val bundle = bundleOf(
                "url" to "https://sites.google.com/view/device-content/home",
                "toolbar" to "Privacy Policy"
            )
            findNavController().navigate(R.id.webViewFragment, bundle)
        }
        binding.engagementLegal2.ivEndIcon.setOnClickListener {
            val bundle = bundleOf(
                "url" to "https://sites.google.com/view/devicecontent/home",
                "toolbar" to "Terms & Conditions"
            )
            findNavController().navigate(R.id.webViewFragment, bundle)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupUI() {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)

        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode
        binding.tvVersion.text = "Version $versionName (Stable)"
        setupEngagement()
        setupLegal()
    }

    private fun setupEngagement() {
        setupRow(
            binding.engagementRateApp,
            "Rate App",
            R.drawable.ic_rate_app,
            R.drawable.ic_arrow_right
        )

        setupRow(
            binding.engagementRateApp2,
            "Feed Back",
            R.drawable.ic_send_feeback,
            R.drawable.ic_arrow_right
        )
    }

    private fun setupLegal() {
        setupRow(
            binding.engagementLegal,
            "Privacy Policy",
            R.drawable.ic_privacy_policy,
            R.drawable.ic_redirect
        )

        setupRow(
            binding.engagementLegal2,
            "Terms Conditions",
            R.drawable.ic_terms_conditions,
            R.drawable.ic_redirect
        )
    }

    private fun setupRow(
        row: ItemRowBinding,
        title: String,
        startIcon: Int,
        endIcon: Int
    ) {
        row.tvDescription.text = title
        row.ivStartIcon.setImageResource(startIcon)
        row.ivEndIcon.setImageResource(endIcon)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}