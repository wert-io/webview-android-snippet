# Проблема

Не работает кнопка загрузки (upload) документов


# Почему так

- Для работы сайта в `WebView` необходимо включить DOM storage и JavaScript

```kotlin
webView.settings.domStorageEnabled = true
webView.settings.javaScriptEnabled = true
```

- Для того чтобы сработала кнопка загрузки документов нужно переопределись метод `onShowFileChooser` в `WebChromeClient` и вручную вызвать диалог выбора файлов

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

# Решение

1. Установить WebView конфигуратор
   ```groovy
   dependencies {
	  implementation 'com.github.wert-io:webview-android-snippet:1.0.0'
   }
   ```

2. Создать экземпляр `WebViewConfig` во фрагменте (`Fragment`) или активности (`Activity`)

   ```Kotlin
   private val webViewConfigurator = WebViewConfig()
   ```

3. Настроить вашу `webView` с помощью конфигуратора

   ```Kotlin
   webViewConfigurator.configure(this, webView)
   ```

   Метод включит DOM storage и JavaScript в `WebView`, а так же настроит `WebChromeClient` для перехвата событий открытия диалогового окна выбора файлов

4. Передавать в конфигуратор ответы от активностей

   ```Kotlin
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       webViewConfigurator.onActivityResult(requestCode, resultCode, data)
   }
   ```

## Пример:

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



## Способ 2

Если на `WebView` уже установлен какой-либо `WebChromeClient`. Тогда нужно в этом `WebChromeClient` переопределись метод `onShowFileChooser` и обработать вызов через конфигуратор. А так же в WebView необходимо включить DomStorage и JavaScript 
(можно сделать методом `webViewConfigurator.setupWebViewSettings(binding.webView)`)

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
