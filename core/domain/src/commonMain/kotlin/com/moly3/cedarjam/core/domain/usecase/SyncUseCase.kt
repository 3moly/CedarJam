package com.moly3.cedarjam.core.domain.usecase

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.doNothing
import com.moly3.cedarjam.core.domain.func.isMoreThan
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultRaise
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.repository.getTempFileNode
import com.moly3.cedarjam.core.domain.service.AlertService
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.math.abs

data class SyncStatusChannel(
    val message: String,
    val progress: Int,
    val all: Int,
    val fileProgress: Float?
)

class SyncUseCase(
    private val alertService: AlertService,
    private val filesRepo: IFilesRepository
) : ISyncUseCase {

    private val _sendingBranch =
        MutableStateFlow<UIState<SyncStatusChannel, String>>(UIState.Loading)

    override suspend fun clearSending() {
        _sendingBranch.emit(UIState.Loading)
    }

    override fun sendingBranchFlow(): Flow<UIState<SyncStatusChannel, String>> {
        return _sendingBranch.asStateFlow()
    }

    sealed class SyncResult {
        data class MarkSyncLocal(val local: IndexFileDto) : SyncResult()
        data class Upload(val reason: String, val local: IndexFileDto) : SyncResult()
        data class Download(val reason: String, val server: FileItem) : SyncResult()
        data class None(val reason: String) : SyncResult()
    }

    private fun determineAction(local: IndexFileDto?, server: FileItem?): SyncResult {
        fun getTimeDiff(t1: Long, t2: Long) = "${abs(t1 - t2)}ms"

        // Case 1: Only Local exists (Recreated folder hits this first)
        if (server == null && local != null) {
            return if (local.serverSyncStatus != SyncStatus.SYNCED) {
                SyncResult.Upload("New local entry (Status: ${local.serverSyncStatus})", local)
            } else {
                SyncResult.None("Synced locally but missing on server")
            }
        }

        // Case 2: Only Server exists
        if (server != null && local == null) {
            return if (server.isDeleted) {
                SyncResult.None("Deleted on both ends")
            } else {
                SyncResult.Download("New entry on server", server)
            }
        }

        // Case 3: Both exist
        if (server != null && local != null) {
            val isLocalDeleted = local.serverSyncStatus == SyncStatus.DELETED
            val diffMs = getTimeDiff(server.modifiedTime, local.modifiedTime)

            // Deletion logic for FILES only
            if (isLocalDeleted) {
                return if (server.modifiedTime.isMoreThan(local.modifiedTime) && !server.isDeleted) {
                    SyncResult.Download("Server file is newer; restoring local", server)
                } else {
                    SyncResult.Upload("Propagating file deletion", local)
                }
            }

            val serverNewer = server.modifiedTime.isMoreThan(local.modifiedTime)
            val localNewer = local.modifiedTime.isMoreThan(server.modifiedTime)
            val hashMismatch = local.contentHash != server.contentHash
            val typeMismatch = local.isDirectory != server.isDirectory

            return when {
                !server.isDeleted && serverNewer && !local.isDirectory -> SyncResult.Download(
                    "Server is newer ($diffMs)",
                    server
                )

                local.serverSyncStatus != SyncStatus.SYNCED -> {
                    if (localNewer && !hashMismatch) {
                        SyncResult.MarkSyncLocal(local)
                    } else {
                        if (hashMismatch || !local.isDirectory && localNewer && hashMismatch) {
                            val reason = when {
                                local.serverSyncStatus != SyncStatus.SYNCED -> "Local status: ${local.serverSyncStatus}"
                                localNewer -> "Local is newer ($diffMs)"
                                else -> "Hash mismatch"
                            }
                            SyncResult.Upload(reason, local)
                        } else {
                            SyncResult.None("Files are identical")
                        }
                    }
                }

                else -> SyncResult.None("Files are identical")
            }
        }

        return SyncResult.None("Both local and server are null")
    }

    override suspend fun getStatus(workspace: IWorkspaceEnvironment): ResultWrapper<GetSyncStatus, String> {
        return withContext(io) {
            resultBlock(onError = { "" }) {
                val serverFiles = bind(workspace.getServerFiles()).files
                val diskNodes =
                    workspace.getNodes(null).firstOrNull()?.getChildrenOrNull()?.getAll()
                        ?: listOf()
                workspace.updateIndexFilesLocal(diskNodes)
                val indexMap = workspace.getIndexFilesFlow().first()
                    .associateBy { it.relativePath.normalizeText() }
                val serverMap = serverFiles.associateBy { it.relativePath.normalizeText() }
                val allPaths = (indexMap.keys + serverMap.keys).distinct()

                val toDownload = mutableMapOf<String, String>()
                val toUpload = mutableMapOf<String, String>()
                val indexFilesToMarkSync = mutableListOf<IndexFileDto>()

                allPaths.forEach { path ->
                    val local = indexMap[path]
                    val server = serverMap[path]
                    when (val determAction = determineAction(local, server)) {
                        is SyncResult.Download -> toDownload[path] = determAction.reason
                        is SyncResult.None -> Unit
                        is SyncResult.Upload -> toUpload[path] = determAction.reason
                        is SyncResult.MarkSyncLocal -> indexFilesToMarkSync.add(determAction.local)
                    }
                }

                //workspace.syncAllIndexes(indexFilesToMarkSync)

                GetSyncStatus(
                    toDownload = toDownload.toPersistentMap(),
                    toUpload = toUpload.toPersistentMap()
                )
            }
        }
    }

    private suspend fun ResultRaise<String>.uploadAtOneTime(
        workspaceEnv: IWorkspaceEnvironment,
        workspaceAbsolutePath: String,
        importNode: FileTreeNode.File,
        exportArchiveNode: FileTreeNode.File,
        serverFiles: List<FileItem>,

        filesToPackInZip: List<String>,
        filesToUploadMeta: List<FileMetadata>,
        filesToDownloadPaths: List<String>,
    ) {
        emitChannel("pack to zip", 3)
        if (filesToPackInZip.isNotEmpty()) {
            filesRepo.deleteNode(importNode)
            filesRepo.packFilesToZip(
                workspaceAbsolutePath,
                filesToPackInZip,
                importNode.getFullPath()
            )
        }
        val uploadResult = workspaceEnv.uploadSync(
            archiveNode = importNode,
            metadata = filesToUploadMeta.filter { meta ->
                if (meta.isDeleted) {
                    true
                } else {
                    filesToPackInZip.contains(meta.relativePath)
                }
            },
            filesToDownload = filesToDownloadPaths,
            onUpload = { curr, total ->
                emitChannel(
                    "onUpload file",
                    5,
                    total?.let { curr / it.toFloat() })
            },
            onDownload = { curr, total ->
                emitChannel(
                    "onDownload file",
                    6,
                    total?.let { curr / it.toFloat() })
            }
        )
        val responseZipBytes = bind(uploadResult)

        if (responseZipBytes.isNotEmpty()) {
            emitChannel("extract from zip", 6)
            filesRepo.deleteNode(exportArchiveNode)
            filesRepo.createNode(
                workspacePath = workspaceEnv.getWorkspace().absolutePath,
                exportArchiveNode,
                byteArray = responseZipBytes
            ).shouldBeSuccess()
            val extractedFiles =
                filesRepo.unpackZip(
                    serverFiles,
                    exportArchiveNode.getFullPath(),
                    workspaceAbsolutePath
                )
            workspaceEnv.setFilesAsSynced(
                extractedFiles,
                serverNodes = serverFiles
            ).shouldBeSuccess()
        }
    }

    private val CHUNK_LIMIT_BYTES = 25L * 1024L * 1024L // 25 MB

    data class SyncChunk(
        val localFiles: List<IndexFileDto>,
        val serverFiles: List<FileItem>,
        val totalSize: Long
    )

    private fun buildSyncChunks(
        local: List<IndexFileDto>,
        server: List<FileItem>
    ): List<SyncChunk> {

        val allEntries = mutableListOf<Triple<Long?, IndexFileDto?, FileItem?>>()

        local.forEach {
            allEntries += Triple(if (it.isDirectory) 0L else it.size, it, null)
        }
        server.forEach {
            allEntries += Triple(if (it.isDirectory) 0L else it.size, null, it)
        }
        val result = mutableListOf<SyncChunk>()

        var currentLocal = mutableListOf<IndexFileDto>()
        var currentServer = mutableListOf<FileItem>()
        var currentSize = 0L

        fun flushChunk() {
            if (currentLocal.isNotEmpty() || currentServer.isNotEmpty()) {
                result += SyncChunk(
                    localFiles = currentLocal.toList(),
                    serverFiles = currentServer.toList(),
                    totalSize = currentSize
                )
                currentLocal = mutableListOf()
                currentServer = mutableListOf()
                currentSize = 0L
            }
        }

        for ((size, localFile, serverFile) in allEntries) {

            // Null size → treat as huge → standalone chunk
            if (size == null) {
                flushChunk()
                result += SyncChunk(
                    localFiles = localFile?.let { listOf(it) } ?: emptyList(),
                    serverFiles = serverFile?.let { listOf(it) } ?: emptyList(),
                    totalSize = 0L
                )
                continue
            }

            // Big file → standalone chunk
            if (size > CHUNK_LIMIT_BYTES) {
                flushChunk()
                result += SyncChunk(
                    localFiles = localFile?.let { listOf(it) } ?: emptyList(),
                    serverFiles = serverFile?.let { listOf(it) } ?: emptyList(),
                    totalSize = size
                )
                continue
            }

            // Would exceed limit → flush current chunk first
            if (currentSize + size > CHUNK_LIMIT_BYTES) {
                flushChunk()
            }

            // Add to current chunk
            if (localFile != null) currentLocal += localFile
            if (serverFile != null) currentServer += serverFile

            currentSize += size
        }

        flushChunk()

        return result
    }

    override suspend fun syncronize(
        workspaceEnv: IWorkspaceEnvironment,
        isAbsoluteNewLocal: Boolean
    ): ResultWrapper<SyncStatus2, String> {
        emitChannel("init", 1)
        val workspaceAbsolutePath = workspaceEnv.getWorkspace().absolutePath
        val importNode = workspaceEnv.getTempFileNode(
            FileName(
                "import",
                "zip"
            )
        )
        val exportArchiveNode = workspaceEnv.getTempFileNode(
            FileName(
                "export",
                "zip"
            )
        )
        return resultBlock(onError = { "" }) {
            try {
                val serverFiles = bind(workspaceEnv.getServerFiles()).files
                val diskNodesInitial =
                    workspaceEnv.getNodes(null).firstOrNull()?.getChildrenOrNull()?.getAll()
                        ?: listOf()

                workspaceEnv.updateIndexFilesLocal(diskNodesInitial)
                workspaceEnv.updateIndexFiles(diskNodesInitial, serverFiles)
                emitChannel("checked local db", 2)

                processServerDeletions(
                    workspaceEnv,
                    diskNodesInitial.associateBy { it.getRelativePath() },
                    serverFiles
                )

                val indexMap = workspaceEnv.getIndexFilesFlow().first()
                    .associateBy { it.relativePath.normalizeText() }
                val serverMap = serverFiles.associateBy { it.relativePath }
                val allPaths = (indexMap.keys + serverMap.keys).distinct()

                val filesToUploadMeta = mutableListOf<FileMetadata>()
                val filesToPackInZip = mutableListOf<IndexFileDto>()
                val filesToDownloadPaths = mutableMapOf<String, FileItem>()
                val editFilesToSync = mutableListOf<IndexFileDto>()

                if (isAbsoluteNewLocal) {
                    for (item in serverMap) {
                        filesToDownloadPaths[item.key] = item.value
                    }
                } else {
                    for (path in allPaths) {
                        val local = indexMap[path]
                        val server = serverMap[path]

                        val determAction = determineAction(local, server)
                        if (local?.relativePath == "moly3/sqlite.db") {
                            val msg = "" + determAction
                        }
                        when (determAction) {
                            is SyncResult.Download -> {
                                filesToDownloadPaths[path] = determAction.server
                            }

                            is SyncResult.None -> Unit
                            is SyncResult.Upload -> {
                                val meta = determAction.local.toMetadata()
                                val isDeletion =
                                    determAction.local.serverSyncStatus == SyncStatus.DELETED
                                val finalMeta =
                                    if (determAction.local.isDirectory || isDeletion) meta
                                    else meta.copy(
                                        contentHash = filesRepo.getFileHash(
                                            pathWrapper(
                                                workspaceAbsolutePath,
                                                path
                                            ).pathString
                                        )
                                    )
                                filesToUploadMeta.add(finalMeta)
                                editFilesToSync.add(determAction.local)
                                if (!isDeletion) {
                                    filesToPackInZip.add(determAction.local)
                                }
                            }

                            is SyncResult.MarkSyncLocal -> {
                                doNothing()
                            }
                        }
                    }
                }
                if (isAbsoluteNewLocal) {
                    filesToPackInZip.clear()
                    filesToUploadMeta.clear()
//                    ensure(filesToPackInZip.isEmpty()) { "no files need to upload when workspace is new: ${filesToPackInZip}" }
                    val isDeleteToUpload = filesToUploadMeta.filter { d -> d.isDeleted }
                    ensure(isDeleteToUpload.isEmpty()) { "no files need to delete in server when workspace is new: ${isDeleteToUpload}" }
                }
                Logger.e {
                    "to zip: ${
                        filesToPackInZip.map { d -> d.relativePath }.joinToString { it }
                    }"
                }
                val chunks =
                    buildSyncChunks(filesToPackInZip, filesToDownloadPaths.map { d -> d.value })

                var index = 0
                emitChannel("upload to server ${index}/${chunks.size}", 4)
                for (chunk in chunks) {

                    uploadAtOneTime(
                        workspaceEnv = workspaceEnv,
                        workspaceAbsolutePath = workspaceAbsolutePath,
                        importNode = importNode,
                        exportArchiveNode = exportArchiveNode,

                        serverFiles = serverFiles,
                        filesToPackInZip = chunk.localFiles.map { d -> d.relativePath },
                        filesToUploadMeta = filesToUploadMeta,
                        filesToDownloadPaths = chunk.serverFiles.map { d -> d.relativePath }
                    )
                    index++
                    emitChannel("upload to server ${index}/${chunks.size}", 4)
                }
                val myDeletedPaths = filesToUploadMeta
                    .filter { it.isDeleted }
                    .map { it.relativePath }
                if (myDeletedPaths.isNotEmpty()) {
                    workspaceEnv.deleteIndexFiles(myDeletedPaths)
                }
                cleanupTempFiles(importNode.getFullPath(), exportArchiveNode)
                emitChannel("success", 7)

                workspaceEnv.syncDirtyFiles(editFilesToSync)

                val diskNodesInitial2 =
                    workspaceEnv.getNodes(null).firstOrNull()?.getChildrenOrNull()?.getAll()
                        ?: listOf()

                workspaceEnv.updateIndexFilesLocal(diskNodesInitial2)

                if (isAbsoluteNewLocal) {
                    workspaceEnv.syncAllIndexes()
                }
                _sendingBranch.emit(UIState.Loading)
                SyncStatus2(isLoading = false)
            } catch (exc: Exception) {
                _sendingBranch.emit(UIState.Loading)
                alertService.sendMessage(exc.message ?: "Sync failed")
                Logger.e(exc) { "Sync failed: ${exc.message}" }
                raise(exc.message ?: "Unknown sync error")
            }
        }
    }

    private fun processServerDeletions(
        workspace: IWorkspaceEnvironment,
        localNodes: Map<String, FileTreeNode>,
        serverFiles: List<FileItem>
    ): List<String> {
        val abso = workspace.getWorkspace().absolutePath
        val explicitlyDeletedByServer = serverFiles.filter { serverFile ->
            val foundNode = localNodes[serverFile.relativePath]
            serverFile.isDeleted && foundNode != null && serverFile.modifiedTime.isMoreThan(
                foundNode.modifiedTime
            )
        }

        val deletedPaths = mutableListOf<String>()
        if (explicitlyDeletedByServer.isNotEmpty()) {
            val pathsToDelete = explicitlyDeletedByServer.map { it.relativePath }
            val relativePaths = pathsToDelete.map { it }

            for (relativePathToDelete in relativePaths) {
                val absoluteToDelete = pathWrapper(abso, relativePathToDelete).pathString
                val meta =
                    SystemFileSystem.metadataOrNull(Path(absoluteToDelete))
                        ?: continue
                val node = filesRepo.getFileNodeFromFullPath(
                    workspacePath = workspace.getWorkspace().absolutePath,
                    fullPath = absoluteToDelete,
                    isDirectory = meta.isDirectory
                )
                filesRepo.deleteNodeHeavy(node)
            }
            workspace.deleteIndexFiles(pathsToDelete)
            deletedPaths.addAll(pathsToDelete)
        }
        return deletedPaths
    }

    private fun IndexFileDto.toMetadata(): FileMetadata {
        return FileMetadata(
            relativePath = this.relativePath,
            modifiedTime = this.modifiedTime,
            contentHash = this.contentHash ?: "",
            isDeleted = this.serverSyncStatus == SyncStatus.DELETED,
            isDirectory = this.isDirectory
        )
    }

    private suspend fun emitChannel(message: String, progress: Int, fileProgress: Float? = null) {
        _sendingBranch.emit(
            UIState.Success(
                SyncStatusChannel(message, progress, all = 7, fileProgress = fileProgress)
            )
        )
    }

    private fun cleanupTempFiles(importPath: String, exportNode: FileTreeNode) {
        try {
            SystemFileSystem.delete(Path(importPath))
            filesRepo.deleteNode(exportNode)
        } catch (_: Exception) {
        }
    }
}