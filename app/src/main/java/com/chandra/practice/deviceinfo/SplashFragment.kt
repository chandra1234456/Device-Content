package com.chandra.practice.deviceinfo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment() {


    override fun onCreateView(
        inflater : LayoutInflater , container : ViewGroup? ,
        savedInstanceState : Bundle? ,
                             ) : View? {
        return inflater.inflate(R.layout.fragment_splash , container , false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logo = view.findViewById<ImageView>(R.id.logoImageView)
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        logo.startAnimation(fadeIn)

        view.postDelayed({
            findNavController().navigate(R.id.action_splash_to_home)
        }, 2000)
    }

}