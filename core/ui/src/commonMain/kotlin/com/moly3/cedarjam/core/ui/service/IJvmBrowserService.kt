package com.moly3.cedarjam.core.ui.service

interface IJvmBrowserService {

    fun isAlreadyInitialized(): Boolean

    suspend fun initKcef(
        onDownloading: (Float) -> Unit,
        onInit: () -> Unit,
        onRestartRequired: () -> Unit
    )
}