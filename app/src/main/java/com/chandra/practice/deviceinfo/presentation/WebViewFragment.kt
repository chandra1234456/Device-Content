package com.chandra.practice.deviceinfo.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.R

class WebViewFragment : Fragment() {

    companion object {
        private const val ARG_URL = "url"
        private const val TOOL_BAR = "toolbar"

        fun newInstance(url: String,toolbar: String): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
            args.putString(TOOL_BAR, toolbar)
            fragment.arguments = args
            return fragment
        }
    }
    private lateinit var webView: WebView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainLayout = view.findViewById<ConstraintLayout>(R.id.main)

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        arguments?.getString(TOOL_BAR)?.let { title ->
            toolbar.setTitle(title)
        }
        toolbar.setNavigationOnClickListener {
           findNavController().navigate(R.id.introFragment)
        }
        webView = view.findViewById(R.id.webview)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
        }

        webView.webViewClient = WebViewClient()

        arguments?.getString(ARG_URL)?.let { url ->
            webView.loadUrl(url)
        }
    }
    // Optional: handle back navigation inside WebView
    fun canGoBack(): Boolean = webView.canGoBack()
    fun goBack() { if (webView.canGoBack()) webView.goBack() }
}