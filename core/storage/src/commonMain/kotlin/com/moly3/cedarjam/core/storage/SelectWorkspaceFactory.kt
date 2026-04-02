package com.moly3.cedarjam.core.storage

import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.storage.func.createSqlStorage

class SqlStorageFactory constructor(
    private val systemFilesManager: ISystemFilesManager,
    private val applicationProvider: AppContextProvider,
) : ISqlStorage.Factory {
    override fun invoke(workspacePath: String): ISqlStorage {
        return createSqlStorage(
            systemFilesManager = systemFilesManager,
            applicationProvider = applicationProvider,
            workspaceDirectoryPath = workspacePath
        )
    }
}