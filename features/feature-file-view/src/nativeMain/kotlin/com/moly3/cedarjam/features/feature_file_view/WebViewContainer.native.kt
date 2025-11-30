package com.moly3.cedarjam.features.feature_file_view

//@OptIn(ExperimentalForeignApi::class)
//@androidx.compose.runtime.Composable
//actual fun WebViewContainer() {
//    val nsView = remember {
//        val config = WKWebViewConfiguration()
//        val webView = WKWebView(frame = CGRectZero.readValue(), configuration = config)
//
//        val htmlNSString = htmlContent.toNSString()
//        webView.loadHTMLString(htmlNSString, baseURL = null)
//
//        webView
//    }
//
//    // Embed NSView inside Compose
//    NSViewWrapper(nsView)
//}
//
//@Composable
//fun NSViewWrapper(view: WKWebView) {
//    LocalUIView
//    val skiaComponent = LocalAppWindow.current.window.contentView as NSView
//    DisposableEffect(view) {
//        skiaComponent.addSubview(view)
//        view.frame = skiaComponent.bounds
//        view.autoresizingMask = NSViewWidthSizable or NSViewHeightSizable
//
//        onDispose {
//            view.removeFromSuperview()
//        }
//    }
//}
