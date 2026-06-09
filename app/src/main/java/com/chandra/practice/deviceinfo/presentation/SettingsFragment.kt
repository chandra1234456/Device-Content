package com.chandra.practice.deviceinfo.presentation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
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
        setupOnClickEventListeners()
    }

    private fun setupOnClickEventListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.engagementRateApp.root.setOnClickListener {
            rateApp()
        }

        binding.engagementRateApp2.root.setOnClickListener {
            sendFeedback()
        }

        binding.engagementLegal.root.setOnClickListener {
            val bundle = bundleOf(
                "url" to "https://sites.google.com/view/device-content/home",
                "toolbar" to "Privacy Policy"
            )
            findNavController().navigate(R.id.webViewFragment, bundle)
        }

        binding.engagementLegal2.root.setOnClickListener {
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
        
        binding.tvVersion.text = "Version $versionName ($versionCode) - Stable"
        
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
            "Feedback & Support",
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
            "Terms & Conditions",
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

    private fun rateApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}"))
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}"))
                startActivity(intent)
            } catch (anfe: Exception) {
                Toast.makeText(requireContext(), "Google Play Store not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendFeedback() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@deviceinfo.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Device Info Pro")
            }
            startActivity(Intent.createChooser(intent, "Send feedback via..."))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No email client found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}