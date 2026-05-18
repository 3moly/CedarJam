package com.moly3.cedarjam.core.domain.di

import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.IFileHasher

/**
 * Minimal service access for modules that cannot depend on the app Metro graph (e.g. core:ui).
 * Set from [com.moly3.cedarjam.di.initApp] after the graph is created.
 */
object AppServiceLocator {
    lateinit var fileHasher: IFileHasher
        private set

    lateinit var appContextProvider: AppContextProvider
        private set

    fun init(fileHasher: IFileHasher, appContextProvider: AppContextProvider) {
        this.fileHasher = fileHasher
        this.appContextProvider = appContextProvider
    }
}
