package io.wert.webviewconfig

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.lang.ref.WeakReference

/**
 * Конфигуратор WebView для работы с сайтом
 * **/
class WebViewConfig: LifecycleObserver {
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var webChromeClient: FileChooserWebChromeClient? = null

    /**
     * Включает DOM storage и JavaScript в WebView,
     * а так же настроит WebChromeClient для перехвата событий открытия диалогового окна выбора файлов
     * **/
    fun configure(activity: ComponentActivity, webView: WebView) {
        clear()
        activity.lifecycle.addObserver(this)
        setupWebViewSettings(webView)
        webChromeClient = getWebViewChromeClient(activity)
        webView.webChromeClient = webChromeClient
    }

    /**
     * Включает DOM storage и JavaScript в WebView,
     * а так же настроит WebChromeClient для перехвата событий открытия диалогового окна выбора файлов
     * **/
    fun configure(fragment: Fragment, webView: WebView) {
        clear()
        fragment.lifecycle.addObserver(this)
        setupWebViewSettings(webView)
        webChromeClient = getWebViewChromeClient(fragment)
        webView.webChromeClient = webChromeClient
    }

    /**
     * Включает DOM storage и JavaScript в WebView
     * **/
    fun setupWebViewSettings(webView: WebView) {
        with(webView.settings) {
            domStorageEnabled = true
            javaScriptEnabled = true
        }
    }

    /**
     * Обрабатывает метод onShowFileChooser из WebChromeClient
     * Создает интент для открытия окна выбора файлов
     * Запускает интент для получения результата по коду [FILE_CHOOSER_REQUEST_CODE] через [activity]
     * **/
    fun handleFileChooser(
        activity: Activity,
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Boolean {
        val intent = createIntent(webView, filePathCallback, fileChooserParams)

        this.filePathCallback = filePathCallback

        try {
            activity.startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
        } catch (ex: Exception) {
            this.filePathCallback = null
        }

        return true
    }

    /**
     * Обрабатывает метод onShowFileChooser из WebChromeClient
     * Создает интент для открытия окна выбора файлов
     * Запускает интент для получения результата по коду [FILE_CHOOSER_REQUEST_CODE] через [fragment]
     * **/
    fun handleFileChooser(
        fragment: Fragment,
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Boolean {
        val intent = createIntent(webView, filePathCallback, fileChooserParams)

        this.filePathCallback = filePathCallback

        try {
            fragment.startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
        } catch (ex: Exception) {
            this.filePathCallback = null
        }

        return true
    }

    /**
     * Ответ от активности выбора файла
     * Парсит картинку из ответного интента и передает обратно в WebChromeClient через [filePathCallback]
     * **/
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val dataString = data?.dataString
            val results = Uri.parse(dataString)
            filePathCallback?.onReceiveValue(arrayOf(results))
        }
    }

    private fun getWebViewChromeClient(fragment: Fragment): FileChooserWebChromeClient {
        return FileChooserWebChromeClient(fragment)
    }

    private fun getWebViewChromeClient(activity: Activity): FileChooserWebChromeClient {
        return FileChooserWebChromeClient(activity)
    }

    private fun createIntent(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Intent {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            return createDefaultIntent()
        }

        val fileChooserIntent = fileChooserParams?.createIntent() ?: return createDefaultIntent()

        val mimeTypes = fileChooserParams.acceptTypes
            ?.filterNotNull()
            ?.map { extension ->
                MimeTypeMap
                    .getSingleton()
                    .getMimeTypeFromExtension(extension.removePrefix("."))
            }
            ?.toTypedArray()

        fileChooserIntent.type = "*/*"
        fileChooserIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        return fileChooserIntent
    }

    private fun createDefaultIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            this.addCategory(Intent.CATEGORY_OPENABLE)
            this.type = "*/*"
            this.putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "image/*",
                    "application/pdf"
                )
            )
        }
    }

    private inner class FileChooserWebChromeClient private constructor(): WebChromeClient() {
        constructor(activity: Activity): this() {
            activityRef = WeakReference(activity)
        }

        constructor(fragment: Fragment): this() {
            fragmentRef = WeakReference(fragment)
        }

        private var activityRef: WeakReference<Activity>? = null
        private var fragmentRef: WeakReference<Fragment>? = null

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            val activity = activityRef?.get()
            val fragment = fragmentRef?.get()

            if (activity != null) {
                return handleFileChooser(
                    activity,
                    webView,
                    filePathCallback,
                    fileChooserParams
                )
            }

            if (fragment != null) {
                return handleFileChooser(
                    fragment,
                    webView,
                    filePathCallback,
                    fileChooserParams
                )
            }

            return false
        }

        fun clear() {
            activityRef?.clear()
            fragmentRef?.clear()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clear() {
        webChromeClient?.clear()
        filePathCallback = null
        webChromeClient = null
    }

    companion object {
        const val FILE_CHOOSER_REQUEST_CODE = 227
    }
}