package com.moly3.cedarjam.shared.repository

import com.moly3.cedarjam.core.ui.service.IJvmBrowserService

actual fun getJvmBrowserService(): IJvmBrowserService {
   return object : IJvmBrowserService {
       override fun isAlreadyInitialized(): Boolean {
           return false
       }

       override suspend fun initKcef(
           onDownloading: (Float) -> Unit,
           onInit: () -> Unit,
           onRestartRequired: () -> Unit
       ) {

       }
   }
}