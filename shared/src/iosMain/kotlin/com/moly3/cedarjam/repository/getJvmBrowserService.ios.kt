package com.moly3.cedarjam.repository

import com.moly3.cedarjam.core.ui.service.IJvmBrowserService

actual fun getJvmBrowserService(): IJvmBrowserService {
    return object: IJvmBrowserService{
        override fun isAlreadyInitialized(): Boolean {
            return true
        }

        override suspend fun initKcef(
            onDownloading: (Float) -> Unit,
            onInit: () -> Unit,
            onRestartRequired: () -> Unit
        ) {

        }

    }
}