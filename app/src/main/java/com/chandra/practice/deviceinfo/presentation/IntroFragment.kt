package com.chandra.practice.deviceinfo.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.databinding.FragmentIntroBinding


class IntroFragment : Fragment() {

    private lateinit var introBinding: FragmentIntroBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        introBinding = FragmentIntroBinding.inflate(layoutInflater, container, false)
        return introBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        introBinding.btnExit.setOnClickListener {
            requireActivity().finish()
        }
        introBinding.txtTerms.setOnClickListener {
            val bundle = bundleOf("url" to "https://sites.google.com/view/devicecontent/home","toolbar" to "Terms")
            findNavController().navigate(R.id.webViewFragment, bundle)
        }
        introBinding.txtPrivacyPolicy.setOnClickListener {
            val bundle = bundleOf("url" to "https://sites.google.com/view/device-content/home","toolbar" to "Privacy")
            findNavController().navigate(R.id.webViewFragment, bundle)
        }
    }
}

private fun Any.beginTransaction() {
    TODO("Not yet implemented")
}
