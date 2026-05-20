package com.moly3.cedarjam.core.data.di

import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.IFileHasher
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry

/**
 * Application service surface for UI/store layers that cannot depend on the `shared` Metro graph
 * (avoids Gradle cycles like shared → page-workspace → page-collection → shared).
 */
interface AppGraphServices {
    val appContextProvider: AppContextProvider
    val fileHasher: IFileHasher
    val syncUseCase: ISyncUseCase
}

object AppGraphServicesLocator {
    lateinit var instance: AppGraphServices
}
