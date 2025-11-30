package com.moly3.cedarjam.features.feature_file_view

import com.multiplatform.webview.web.NativeWebView

expect suspend fun evaluate(native: NativeWebView, code: String)