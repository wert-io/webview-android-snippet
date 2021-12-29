# Problem

Document upload button (upload) is not working

# Why it happened

- DOM storage and JavaScript should be enabled so that website could work in `WebView`

```kotlin
webView.settings.domStorageEnabled = true
webView.settings.javaScriptEnabled = true
```

- `onShowFileChooser` method should be overridden with `WebChromeClient`, file selection dialogue should be requested manually for the upload button to work

```Kotlin
webView.webChromeClient = object : WebChromeClient() {
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        // Handle file chooser
    }
}
```

# Solution

1. Set up WebView configurator
   ```groovy
   dependencies {
	  implementation 'com.github.wert-io:webview-android-snippet:1.0.0'
   }
   ```

2. Create a `WebViewConfig` instance in `Fragment` or `Activity`

   ```Kotlin
   private val webViewConfigurator = WebViewConfig()
   ```

3. Set up your `webView` with configurator

   ```Kotlin
   webViewConfigurator.configure(this, webView)
   ```

   The method will enable DOM storage and JavaScript in `WebView` and configure `WebChromeClient` to hook the event of opening a file selection dialogue

4. Pass responses from activities to the configurator

   ```Kotlin
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       webViewConfigurator.onActivityResult(requestCode, resultCode, data)
   }
   ```

## Example:

```Kotlin
class MainFragment : Fragment() {
  	private val webView: WebView by lazy { 
        requireView().findViewById<WebView>(R.id.web_view)
    }
    private val webViewConfigurator = WebViewConfig()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      
        webViewConfigurator.configure(this, webView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        webViewConfigurator.onActivityResult(requestCode, resultCode, data)
    }
  
  	fun loadPage() {
        webView.loadUrl("https://sandbox.wert.io/")
    }
}
```



## Solution 2

 Use when some `WebChromeClient` has already been installed on `WebView`. In that case you need to override the `onShowFileChooser` method in that `WebChromeClient` and process the call through configurator. Also, enable DomStorage and JavaScript. 
(can be done with `webViewConfigurator.setupWebViewSettings(binding.webView)`)

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  
    webViewConfigurator.setupWebViewSettings(webView)
    binding.webView.webChromeClient = object : WebChromeClient() {
      	...
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            return webViewConfigurator.handleFileChooser(
                this@MainFragment,
                webView,
                filePathCallback,
                fileChooserParams
            )
        }
    }
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    webViewConfigurator.onActivityResult(requestCode, resultCode, data)
}
```
