package com.moly3.cedarjam.shared.di

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.di.AppServiceLocator
import com.moly3.cedarjam.core.data.di.AppGraphServicesLocator
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.di.StorageBindingTestMode
import com.moly3.cedarjam.shared.di.metro.createCedarJamAppGraph
import com.moly3.cedarjam.shared.di.metro.CedarJamGraph
import com.moly3.cedarjam.core.storage.func.init
import com.moly3.core_domain.BuildConfig
import io.github.vinceglb.filekit.FileKit

fun initApp(
    context: AndroidApplicationContext,
    isRelease: Boolean = BuildConfig.IsRelease,
    isTest: Boolean = false
) {
    if (isRelease) {
        Logger.setLogWriters(listOf())
        Logger.setMinSeverity(Severity.Assert)
    } else {
        Logger.setLogWriters(CommonWriter())
    }
    FileKit.init(context)
    PlatformAndroidContext.init(context)
    StorageBindingTestMode.useMapSettings = isTest

    CedarJamGraph.init(createCedarJamAppGraph())
    AppGraphServicesLocator.instance = CedarJamGraph.deps
    AppServiceLocator.init(
        fileHasher = CedarJamGraph.deps.fileHasher,
        appContextProvider = CedarJamGraph.deps.appContextProvider,
    )
}
