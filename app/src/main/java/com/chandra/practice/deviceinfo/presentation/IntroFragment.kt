package com.chandra.practice.deviceinfo.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.databinding.FragmentIntroBinding

class IntroFragment : Fragment() {

    private lateinit var introBinding: FragmentIntroBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        introBinding = FragmentIntroBinding.inflate(inflater, container, false)
        return introBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // One-time onboarding check using SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("is_onboarded", false)) {
            findNavController().navigate(R.id.action_intro_to_splash)
            return
        }

        introBinding.btnExit.setOnClickListener {
            requireActivity().finish()
        }

        introBinding.termsAndConditionsText.setOnClickListener {
            val bundle = bundleOf(
                "url" to "https://sites.google.com/view/devicecontent/home",
                "toolbar" to "Terms & Conditions"
            )
            findNavController().navigate(R.id.webViewFragment, bundle)
        }

        introBinding.privacyPolicyText.setOnClickListener {
            val bundle = bundleOf(
                "url" to "https://sites.google.com/view/device-content/home",
                "toolbar" to "Privacy Policy"
            )
            findNavController().navigate(R.id.webViewFragment, bundle)
        }

        introBinding.btnAgree.setOnClickListener {
            // Save onboarding status so this screen is bypassed next time
            sharedPref.edit().putBoolean("is_onboarded", true).apply()
            findNavController().navigate(R.id.action_intro_to_splash)
        }
    }
}
