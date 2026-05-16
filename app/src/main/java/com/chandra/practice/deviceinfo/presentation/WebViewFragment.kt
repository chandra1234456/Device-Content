package com.chandra.practice.deviceinfo.presentation

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.util.LoaderHelper

class WebViewFragment : Fragment() {
    private var webView: WebView? = null
    private var loader: LoaderHelper? = null
    companion object {
        private const val ARG_URL = "url"
        private const val TOOL_BAR = "toolbar"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainLayout = view.findViewById<ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = arguments?.getString(TOOL_BAR) ?: "Web Page"
        toolbar.setNavigationOnClickListener {
            val wv = webView
            if (wv != null && wv.canGoBack()) {
                wv.goBack()
            } else {
                findNavController().navigateUp()
            }
        }

        // ProgressBar removed — fragment will not show inline progress

        webView = view.findViewById(R.id.webview)
        setupWebView()
        // initialize loader helper for showing a modal loading dialog
        loader = LoaderHelper(requireActivity())

        arguments?.getString(ARG_URL)?.let { url ->
            webView?.loadUrl(url)
        }

        // Back press callback tied to the view lifecycle owner
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val wv = webView
            if (wv != null && wv.canGoBack()) {
                wv.goBack()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupWebView() {
        webView?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        webView?.webChromeClient = object : WebChromeClient() {
            // No progress UI. Keep for future chrome-related callbacks.
        }

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // show modal loader while page is loading
                loader?.show()
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // hide modal loader when page finished
                loader?.hide()
                super.onPageFinished(view, url)
            }

            // Deprecated callback for older devices
            @Suppress("DEPRECATION")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                loader?.hide()
                Toast.makeText(requireContext(), "Failed to load page", Toast.LENGTH_SHORT).show()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // hide loader and show toast for main frame failures
                loader?.hide()
                if (request?.isForMainFrame != false) {
                    Toast.makeText(requireContext(), "Failed to load page", Toast.LENGTH_SHORT).show()
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme ?: return false
                return if (scheme.startsWith("http")) {
                    // Let WebView handle http/https URLs
                    false
                } else {
                    // For non-http schemes (tel:, mailto:, intent:) open external apps
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Cannot open link", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        webView?.apply {
            try {
                stopLoading()
                clearHistory()
                clearCache(true)
                loadUrl("about:blank")
                // remove from parent to avoid window leaks
                (parent as? ViewGroup)?.removeView(this)
                removeAllViews()
              //  webViewClient = null
                webChromeClient = null
                destroy()
            } catch (t: Throwable) {
                // ignore cleanup errors
            }
        }
        // ensure loader dialog is dismissed if still showing
        loader?.hide()
        loader = null
        webView = null
        super.onDestroyView()
    }
}