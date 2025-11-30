package com.moly3.cedarjam.core.storage.di

import com.moly3.cedarjam.core.storage.IAppStorage
import com.moly3.cedarjam.core.storage.internal.AppStorage
import com.moly3.cedarjam.core.storage.ISqlStorage
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.createSqlStorage
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

fun db(isTest: Boolean) = module {
    single<Settings> { if (isTest) MapSettings() else Settings() }
    single<IAppStorage> { AppStorage(keyValueSettings = get()) }
    single<ISystemFilesManager> { createSystemFilesManager() }
    factory<ISqlStorage> { (workspacePath: String) ->
        createSqlStorage(
            systemFilesManager = get(),
            applicationProvider = get(),
            workspaceDirectoryPath = workspacePath
        )
    }
}