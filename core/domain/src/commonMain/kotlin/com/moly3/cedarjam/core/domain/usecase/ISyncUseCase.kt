package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment

interface ISyncUseCase {
    suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus2, String>
    suspend fun getStatus(workspace: IWorkspaceEnvironment): ResultWrapper<Unit, String>
}

data class SyncStatus2(
    val filesDownloaded: List<String> = listOf(),
    val filesToDownload: List<String> = listOf(),
    val localDeletedFilesByServer: List<FileTreeNode> = listOf(),
    val filesToArchive: List<FileMetadata> = listOf()
)