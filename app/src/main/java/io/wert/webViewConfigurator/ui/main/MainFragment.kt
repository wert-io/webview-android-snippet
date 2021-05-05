package io.wert.webViewConfigurator.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.wert.webViewConfigurator.R
import io.wert.webViewConfigurator.databinding.MainFragmentBinding
import io.wert.webviewconfig.WebViewConfig

class MainFragment : Fragment() {
    private lateinit var binding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel

    private val webViewConfigurator = WebViewConfig()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        binding = MainFragmentBinding.bind(view)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webViewConfigurator.configure(this, binding.webView)
        loadPage()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        webViewConfigurator.onActivityResult(requestCode, resultCode, data)
    }

    fun loadPage() {
        binding.webView.loadUrl(TEST_URL)
    }

    companion object {
        const val TEST_URL = "https://sandbox.wert.io/"
        fun newInstance() = MainFragment()
    }

}