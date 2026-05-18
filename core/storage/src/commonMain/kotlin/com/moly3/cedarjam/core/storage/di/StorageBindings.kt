package com.moly3.cedarjam.core.storage.di

import com.moly3.cedarjam.core.domain.service.IFileHasher
import com.moly3.cedarjam.core.storage.IAppStorage
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.moly3.cedarjam.core.storage.internal.AppStorage
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object StorageBindings {

    @SingleIn(AppScope::class)
    @Provides
    fun provideSettings(): Settings {
        return if (StorageBindingTestMode.useMapSettings) {
            MapSettings()
        } else {
            Settings()
        }
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideIAppStorage(settings: Settings): IAppStorage {
        return AppStorage(keyValueSettings = settings)
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideSystemFilesManager(): ISystemFilesManager {
        return createSystemFilesManager()
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideFileHasher(systemFilesManager: ISystemFilesManager): IFileHasher {
        return systemFilesManager
    }
}

/** Set by application `initApp` before the Metro graph is created (tests use Map settings). */
object StorageBindingTestMode {
    var useMapSettings: Boolean = false
}



