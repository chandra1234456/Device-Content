package com.chandra.practice.deviceinfo.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.chandra.practice.deviceinfo.R

class LoaderHelper(private val activity: Activity) {

    private var dialog: Dialog? = null

    fun show(message: String = "Please wait...") {
        if (activity.isFinishing) return

        if (dialog == null) {
            dialog = Dialog(activity).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.custom_progress_loading)
                setCancelable(false)

                window?.setBackgroundDrawableResource(
                    R.drawable.bg_loader_inset
                )
            }
        }

        dialog?.findViewById<TextView>(R.id.tvLoadingText)?.text = message

        if (dialog?.isShowing == false) {
            dialog?.show()
        }

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun hide() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        dialog = null
    }
}