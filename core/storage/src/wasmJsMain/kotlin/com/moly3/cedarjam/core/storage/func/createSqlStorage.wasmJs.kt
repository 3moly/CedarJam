package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.storage.DemoStorageImpl
import com.moly3.cedarjam.core.storage.ISqlStorage
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.domain.service.AppContextProvider

actual fun createSqlStorage(
    systemFilesManager: ISystemFilesManager,
    applicationProvider: AppContextProvider,
    workspaceDirectoryPath: String
): ISqlStorage {
    return DemoStorageImpl(
        applicationProvider = applicationProvider,
        workspaceDirectoryPath = workspaceDirectoryPath
    )
}