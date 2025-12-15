package com.moly3.cedarjam.core.net

import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileStructure
import com.moly3.cedarjam.core.domain.model.ResultWrapper

interface IRemoteSyncRepository {
    suspend fun upload(
        userName: String,
        workspaceName: String,
        archiveByteArray: ByteArray?,
        metadata: List<FileMetadata>,
        filesToDownload: List<String>,
        onDownload: suspend (Long, Long?) -> Unit,
        onUpload: suspend (Long, Long?) -> Unit
    ): ResultWrapper<ByteArray, String>

    suspend fun workspaceFiles(
        userName: String,
        workspaceName: String
    ): ResultWrapper<FileStructure, String>

    suspend fun deleteWorkspace(
        userName: String,
        workspaceName: String
    ): ResultWrapper<Unit, String>
}