package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment

interface ISyncUseCase {
    suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus2, String>
    suspend fun getStatus(workspace: IWorkspaceEnvironment)
}

data class SyncStatus2(
    val filesDownloaded: List<String>,
    val filesToDownload: List<String>,
    val localDeletedFilesByServer: List<FileTreeNode>,
    val filesToArchive: List<FileMetadata>
)