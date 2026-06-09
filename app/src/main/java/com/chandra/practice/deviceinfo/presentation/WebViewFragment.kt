package com.chandra.practice.deviceinfo.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chandra.practice.deviceinfo.databinding.FragmentWebViewBinding

class WebViewFragment : Fragment() {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_URL = "url"
        private const val TOOL_BAR = "toolbar"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adjust for system status/navigation bars padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        setupToolbar()
        setupWebView()
        setupBackPressCallback()

        // Load URL from fragment arguments
        arguments?.getString(ARG_URL)?.let { url ->
            binding.webview.loadUrl(url)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = arguments?.getString(TOOL_BAR) ?: "Web Page"
        binding.toolbar.setNavigationOnClickListener {
            if (binding.webview.canGoBack()) {
                binding.webview.goBack()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (_binding == null) return
                binding.progressIndicator.apply {
                    if (newProgress < 100) {
                        visibility = View.VISIBLE
                        progress = newProgress
                    } else {
                        visibility = View.GONE
                    }
                }
            }
        }

        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (_binding != null) {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (_binding != null) {
                    binding.progressIndicator.visibility = View.GONE
                }
                super.onPageFinished(view, url)
            }

            @Suppress("DEPRECATION")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (_binding != null) {
                    binding.progressIndicator.visibility = View.GONE
                }
                Toast.makeText(requireContext(), "Failed to load page", Toast.LENGTH_SHORT).show()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (_binding != null) {
                    binding.progressIndicator.visibility = View.GONE
                }
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
                    false // Let WebView handle it
                } else {
                    // Open in external default app (tel:, mailto:, map:, etc.)
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

    private fun setupBackPressCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (_binding != null && binding.webview.canGoBack()) {
                binding.webview.goBack()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            binding.webview.onResume()
        }
    }

    override fun onPause() {
        if (_binding != null) {
            binding.webview.onPause()
        }
        super.onPause()
    }

    override fun onDestroyView() {
        if (_binding != null) {
            binding.webview.apply {
                try {
                    stopLoading()
                    clearHistory()
                    clearCache(true)
                    loadUrl("about:blank")
                    (parent as? ViewGroup)?.removeView(this)
                    removeAllViews()
                    webChromeClient = null
                   // webViewClient = null
                    destroy()
                } catch (t: Throwable) {
                    // ignore cleanup errors
                }
            }
        }
        super.onDestroyView()
        _binding = null
    }
}