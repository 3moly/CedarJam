package com.moly3.cedarjam.core.domain.usecase

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.isMoreThan
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
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

    enum class SyncAction { UPLOAD, DOWNLOAD, NONE }

    /**
     * Unified logic for deciding what to do with a file.
     * This is used by both getStatus() and invoke().
     */
    data class SyncResult(
        val action: SyncAction,
        val reason: String
    )

    //    private fun determineAction(local: IndexFileDto?, server: FileItem?): SyncResult {
//        fun getTimeDiff(t1: Long, t2: Long) = "${abs(t1 - t2)}ms"
//
//        // Case 1: Only Local exists
//        if (server == null && local != null) {
//            return if (local.serverSyncStatus != SyncStatus.SYNCED) {
//                SyncResult(SyncAction.UPLOAD, "New local file/folder")
//            } else {
//                SyncResult(SyncAction.NONE, "Locally synced but missing on server (External deletion)")
//            }
//        }
//
//        // Case 2: Only Server exists
//        if (server != null && local == null) {
//            return if (server.isDeleted) {
//                SyncResult(SyncAction.NONE, "Deleted on server and local")
//            } else {
//                SyncResult(SyncAction.DOWNLOAD, "New file on server")
//            }
//        }
//
//        // Case 3: Both exist
//        if (server != null && local != null) {
//            val isLocalDeleted = local.serverSyncStatus == SyncStatus.DELETED
//            val diffMs = getTimeDiff(server.modifiedTime, local.modifiedTime)
//
//            val serverNewer = server.modifiedTime > local.modifiedTime
//            val localNewer = local.modifiedTime > server.modifiedTime
//            val hashMismatch = local.contentHash != server.contentHash
//            val localModified = local.serverSyncStatus != SyncStatus.SYNCED
//
//            // Handle Local Deletion Logic
//            if (isLocalDeleted) {
//                return if (serverNewer && !server.isDeleted) {
//                    SyncResult(SyncAction.DOWNLOAD, "Server has newer version; reviving local")
//                } else {
//                    SyncResult(SyncAction.UPLOAD, "Syncing deletion to server")
//                }
//            }
//
//            return when {
//                // Case: Server is newer and local hasn't changed (Clean Download)
//                serverNewer && !localModified -> {
//                    SyncResult(SyncAction.DOWNLOAD, "Server version is newer (diff: $diffMs)")
//                }
//
//                // Case: Local is newer OR local is modified/new folder (Upload)
//                localModified || localNewer || hashMismatch -> {
//                    val reason = when {
//                        localModified -> "Local changes detected (Status: ${local.serverSyncStatus})"
//                        localNewer -> "Local version is newer"
//                        else -> "Hash mismatch"
//                    }
//
//                    // FIXED: Allow directory uploads if they are modified or new
//                    SyncResult(SyncAction.UPLOAD, reason)
//                }
//
//                else -> SyncResult(SyncAction.NONE, "Files are synchronized")
//            }
//        }
//
//        return SyncResult(SyncAction.NONE, "No files to sync")
//    }
    private fun determineAction(local: IndexFileDto?, server: FileItem?): SyncResult {
        fun getTimeDiff(t1: Long, t2: Long) = "${abs(t1 - t2)}ms"

        // Case 1: Only Local exists (Recreated folder hits this first)
        if (server == null && local != null) {
            return if (local.serverSyncStatus != SyncStatus.SYNCED) {
                SyncResult(SyncAction.UPLOAD, "New local entry (Status: ${local.serverSyncStatus})")
            } else {
                SyncResult(SyncAction.NONE, "Synced locally but missing on server")
            }
        }

        // Case 2: Only Server exists
        if (server != null && local == null) {
            return if (server.isDeleted) {
                SyncResult(SyncAction.NONE, "Deleted on both ends")
            } else {
                SyncResult(SyncAction.DOWNLOAD, "New entry on server")
            }
        }

        // Case 3: Both exist
        if (server != null && local != null) {
            // FIX: The "Untouchable" Rule
            // If it's a directory and exists on both, we exit immediately.
//            if (local.isDirectory ){
//                return SyncResult(
//                    SyncAction.NONE,
//                    "Directory exists on both ends; metadata ignored"
//                )
//            }

            val isLocalDeleted = local.serverSyncStatus == SyncStatus.DELETED
            val diffMs = getTimeDiff(server.modifiedTime, local.modifiedTime)

            // Deletion logic for FILES only
            if (isLocalDeleted) {
                return if (server.modifiedTime.isMoreThan(local.modifiedTime) && !server.isDeleted) {
                    SyncResult(SyncAction.DOWNLOAD, "Server file is newer; restoring local")
                } else {
                    SyncResult(SyncAction.UPLOAD, "Propagating file deletion")
                }
            }

            val serverNewer = server.modifiedTime.isMoreThan(local.modifiedTime)
            val localNewer = local.modifiedTime.isMoreThan(server.modifiedTime)
            val hashMismatch = local.contentHash != server.contentHash
            val typeMismatch = local.isDirectory != server.isDirectory

            return when {
                serverNewer && !local.isDirectory -> SyncResult(
                    SyncAction.DOWNLOAD,
                    "Server is newer ($diffMs)"
                )

                local.serverSyncStatus != SyncStatus.SYNCED || !local.isDirectory && localNewer || hashMismatch -> {
                    val reason = when {
                        local.serverSyncStatus != SyncStatus.SYNCED -> "Local status: ${local.serverSyncStatus}"
                        localNewer -> "Local is newer ($diffMs)"
                        else -> "Hash mismatch"
                    }
                    SyncResult(SyncAction.UPLOAD, reason)
                }

                else -> SyncResult(SyncAction.NONE, "Files are identical")
            }
        }

        return SyncResult(SyncAction.NONE, "Both local and server are null")
    }

    override suspend fun getStatus(workspace: IWorkspaceEnvironment): ResultWrapper<GetSyncStatus, String> {
        return withContext(io) {
            resultBlock {
                val serverFiles = bind(workspace.getServerFiles()).files
                val diskNodes =
                    workspace.getNodes(null).firstOrNull()?.getChildrenOrNull()?.getAll()
                        ?: listOf()

                // Sync DB state with physical disk and server meta
                workspace.updateIndexFilesLocal(diskNodes)
//                workspace.updateIndexFiles(diskNodes, serverFiles)

                val indexMap = workspace.getIndexFilesFlow().first()
                    .associateBy { it.relativePath.normalizeText() }
                val serverMap = serverFiles.associateBy { it.relativePath.normalizeText() }
                val allPaths = (indexMap.keys + serverMap.keys).distinct()

                val toDownload = mutableMapOf<String, String>()
                val toUpload = mutableMapOf<String, String>()

                allPaths.forEach { path ->
                    val local = indexMap[path]
                    val server = serverMap[path]
                    val determAction = determineAction(local, server)
                    when (determAction.action) {
                        SyncAction.DOWNLOAD -> toDownload[path] = determAction.reason
                        SyncAction.UPLOAD -> toUpload[path] = determAction.reason
                        SyncAction.NONE -> Unit
                    }
                }

                GetSyncStatus(
                    toDownload = toDownload.toPersistentMap(),
                    toUpload = toUpload.toPersistentMap()
                )
            }
        }
    }

    override suspend fun syncronize(
        workspaceEnv: IWorkspaceEnvironment,
        isAbsoluteNewLocal: Boolean
    ): ResultWrapper<SyncStatus2, String> {
        emitChannel("init", 1)
        val workspaceAbsolutePath = workspaceEnv.getWorkspace().absolutePath
        val hiddenDirPath = pathWrapper(hiddenDirectory).pathString
        val importNode =
            FileTreeNode.File(
                FileName(
                    "import",
                    "zip"
                ),
                workspaceAbsolutePath,
                hiddenDirPath
            )
        val exportArchiveNode =
            FileTreeNode.File(
                FileName(
                    "export",
                    "zip"
                ),
                workspaceAbsolutePath,
                hiddenDirPath
            )

        return resultBlock {
            try {
                filesRepo.deleteNode(importNode)
                // 1. Refresh Metadata
                val serverFiles = bind(workspaceEnv.getServerFiles()).files
                val diskNodesInitial =
                    workspaceEnv.getNodes(null).firstOrNull()?.getChildrenOrNull()?.getAll()
                        ?: listOf()

                workspaceEnv.updateIndexFilesLocal(diskNodesInitial)
                workspaceEnv.updateIndexFiles(diskNodesInitial, serverFiles)
                emitChannel("checked local db", 2)

                // 2. Handle Explicit Server Deletions
                processServerDeletions(
                    workspaceEnv,
                    diskNodesInitial.associateBy { it.getRelativePath() },
                    serverFiles
                )

                // 3. Evaluation against Database
                val indexMap = workspaceEnv.getIndexFilesFlow().first()
                    .associateBy { it.relativePath.normalizeText() }
                val serverMap = serverFiles.associateBy { it.relativePath.normalizeText() }
                val allPaths = (indexMap.keys + serverMap.keys).distinct()

                val filesToUploadMeta = mutableListOf<FileMetadata>()
                val filesToPackInZip = mutableListOf<String>()
                val filesToDownloadPaths = mutableListOf<String>()
                val editFilesToSync = mutableListOf<IndexFileDto>()

                if (isAbsoluteNewLocal) {
                    for (item in serverMap) {
                        filesToDownloadPaths.add(item.value.relativePath.normalizeText())
                    }
                } else {
                    for (path in allPaths) {
                        val local = indexMap[path]
                        val server = serverMap[path]

                        val determAction = determineAction(local, server)
                        when (determAction.action) {
                            SyncAction.UPLOAD -> {
                                local?.let {
//                                filesToUploadMeta.add(finalMeta)
//                                filesToPackInZip.add(path)
                                    val meta = it.toMetadata()
                                    val isDeletion = it.serverSyncStatus == SyncStatus.DELETED
//
//                                // Refresh hash for existing local files
                                    val finalMeta = if (it.isDirectory || isDeletion) meta
                                    else meta.copy(
                                        contentHash = filesRepo.getFileHash(
                                            pathWrapper(
                                                workspaceAbsolutePath,
                                                path
                                            ).pathString
                                        )
                                    )
//
                                    filesToUploadMeta.add(finalMeta)
                                    editFilesToSync.add(it)
                                    if (!isDeletion) {
                                        filesToPackInZip.add(path)
                                    }

//                                // Only pack into zip if it's a file, not deleted, and server doesn't have it
//                                if (!it.isDirectory && !isDeletion && (server == null || server.contentHash != finalMeta.contentHash)) {
//                                    filesToPackInZip.add(path)
//                                }
                                }
                            }

                            SyncAction.DOWNLOAD -> {
                                filesToDownloadPaths.add(path.normalizeText())
                            }

                            SyncAction.NONE -> Unit
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

                // 4. Execution: Zipping and Uploading
                emitChannel("pack to zip", 3)
                if (filesToPackInZip.isNotEmpty()) {
                    filesRepo.packFilesToZip(
                        workspaceAbsolutePath,
                        filesToPackInZip,
                        importNode.getFullPath()
                    )
                }

                emitChannel("upload to server", 4)
                val uploadResult = workspaceEnv.uploadSync(
                    archiveNode = importNode,
                    metadata = filesToUploadMeta,
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

                // Update database for local changes
                workspaceEnv.syncDirtyFiles(editFilesToSync)

                // Clean up entries for locally deleted files that were confirmed by server
                val myDeletedPaths =
                    filesToUploadMeta.filter { it.isDeleted }.map { it.relativePath }
                if (myDeletedPaths.isNotEmpty()) {
                    workspaceEnv.deleteIndexFiles(myDeletedPaths)
                }

                // 5. Execution: Unpacking Downloads
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
                        serverNodes = serverMap.values.toList()
                    ).shouldBeSuccess()
                }

                cleanupTempFiles(importNode.getFullPath(), exportArchiveNode)
                emitChannel("success", 7)

                val diskNodesInitial2 =
                    workspaceEnv.getNodes(null).firstOrNull()?.getChildrenOrNull()?.getAll()
                        ?: listOf()

                workspaceEnv.updateIndexFilesLocal(diskNodesInitial2)

                if (isAbsoluteNewLocal) {
                    workspaceEnv.syncAllIndexes()
                }


                SyncStatus2(isLoading = false)
            } catch (exc: Exception) {
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