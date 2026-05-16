package com.chandra.practice.deviceinfo.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.R

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?,
        savedInstanceState : Bundle?,
                             ) : View? {
        return inflater.inflate(R.layout.fragment_splash , container , false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val logo = view.findViewById<ImageView>(R.id.logoImageView)
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        logo.startAnimation(fadeIn)

        // animate the linear progress indicator from 0 -> 100 over the splash duration
        val progress = view.findViewById<LinearProgressIndicator>(R.id.linearIndicator)
        progress?.let {
            it.isIndeterminate = false
            it.max = 100
            it.progress = 0
            ObjectAnimator.ofInt(it, "Progress", 0, 100).apply {
                duration = 2000L
                interpolator = LinearInterpolator()
                start()
            }
        }

        view.postDelayed({
            findNavController().navigate(R.id.action_splash_to_home)
        }, 2000)
    }

}