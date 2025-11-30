package com.moly3.cedarjam.features.feature_browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService

@Composable
fun BrowserPreInitUI(
    jvmBrowserService: IJvmBrowserService,
    content: @Composable () -> Unit
) {
    var initialized by remember(jvmBrowserService) { mutableStateOf(jvmBrowserService.isAlreadyInitialized()) }

    if (initialized) {
        content()
    }

    LaunchedEffect(Unit) {
        if (jvmBrowserService.isAlreadyInitialized())
            return@LaunchedEffect

        jvmBrowserService.initKcef(
            onDownloading = {

            },
            onInit = {
                initialized = true
            },
            onRestartRequired = {}
        )
    }
}