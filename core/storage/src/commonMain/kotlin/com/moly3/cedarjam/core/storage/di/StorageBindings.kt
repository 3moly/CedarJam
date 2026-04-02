package com.moly3.cedarjam.core.storage.di

import com.moly3.cedarjam.core.domain.service.IFileHasher
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

// single<Settings> { if (isTest) MapSettings() else Settings() }
//    single<IAppStorage> { AppStorage(keyValueSettings = get()) }
//    single<ISystemFilesManager> { createSystemFilesManager() }
//    single<IFileHasher> { get<ISystemFilesManager>() }
//    factory<ISqlStorage> { (workspacePath: String) ->
//        createSqlStorage(
//            systemFilesManager = get(),
//            applicationProvider = get(),
//            workspaceDirectoryPath = workspacePath
//        )
//    }

@ContributesTo(AppScope::class)
@BindingContainer
object StorageBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideSettings(): Settings {
        return Settings()
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideSystemFilesManager(): ISystemFilesManager {
        return createSystemFilesManager()
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideFileHasher(): IFileHasher {
        return createSystemFilesManager()
    }
}