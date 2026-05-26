package com.moly3.cedarjam.shared.repository

import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

actual fun getJvmBrowserService(): IJvmBrowserService {
    return object : IJvmBrowserService {
        private var isInit = false

        override fun isAlreadyInitialized(): Boolean {
            return isInit
        }

        override suspend fun initKcef(
            onDownloading: (Float) -> Unit,
            onInit: () -> Unit,
            onRestartRequired: () -> Unit
        ) {
            withContext(io) {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    this.appHandler(
                        KCEF.AppHandler(
                            arrayOf(
                                "--allow-file-access-from-files",
                                "--disable-web-security",
                                "--disable-gpu",
                                "--enable-transparent-painting",
                            )
                        )
                    )
                    progress {
                        onDownloading {
                            onDownloading(max(it, 0F))
                        }
                        onInitialized {
                            isInit = true
                            onInit()
                        }
                    }
                    settings {
                        cachePath = File("cache").absolutePath
                    }
                }, onError = {
                    it?.printStackTrace()
                }, onRestartRequired = {
                    onRestartRequired()
                })
            }
        }
    }
}