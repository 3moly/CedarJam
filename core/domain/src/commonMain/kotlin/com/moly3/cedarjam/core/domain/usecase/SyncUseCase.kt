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

    private enum class SyncAction { UPLOAD, DOWNLOAD, NONE }

    /**
     * Unified logic for deciding what to do with a file.
     * This is used by both getStatus() and invoke().
     */
    private fun determineAction(local: IndexFileDto?, server: FileItem?): SyncAction {
        // Case 1: File exists only locally (or marked as local delete)
        if (server == null && local != null) {
            return if (local.serverSyncStatus != SyncStatus.SYNCED) SyncAction.UPLOAD else SyncAction.NONE
        }

        // Case 2: File exists only on server
        if (server != null && local == null) {
            return if (server.isDeleted)
                SyncAction.NONE
            else
                SyncAction.DOWNLOAD
        }
        val isLocalDeleted = local?.serverSyncStatus == SyncStatus.DELETED
        // Case 3: File exists in both places
        if (server != null && local != null) {
            if (isLocalDeleted) {
                // Only download if the server has a NEWER version than our deletion record
                // otherwise, tell the server to delete its copy too.
                return if (server.modifiedTime.isMoreThan(local.modifiedTime) && !server.isDeleted) {
                    SyncAction.DOWNLOAD
                } else {
                    SyncAction.UPLOAD
                }
            }

            val serverNewer = server.modifiedTime.isMoreThan(local.modifiedTime)
            val localNewer = local.modifiedTime.isMoreThan(server.modifiedTime)
            val hashMismatch = local.contentHash != server.contentHash
            val typeMismatch = local.isDirectory != server.isDirectory

            return when {
                // Server wins: Type mismatch or server is strictly newer
//                typeMismatch || (serverNewer && !local.isDirectory) -> {
                serverNewer -> {
                    SyncAction.DOWNLOAD
                }

                // Local wins: Local has pending changes or is strictly newer

                local.serverSyncStatus != SyncStatus.SYNCED || localNewer || hashMismatch -> {
                    // Если это папка и она помечена как удаленная,
                    // и при этом она новее сервера (localNewer уже вычислен выше),
                    // разрешаем SyncAction.UPLOAD
                    if (local.isDirectory) {
                        if (local.serverSyncStatus == SyncStatus.DELETED && localNewer) {
                            SyncAction.UPLOAD
                        } else {
                            SyncAction.NONE
                        }
                    } else {
                        SyncAction.UPLOAD
                    }
                }

                else -> SyncAction.NONE
            }
        }

        return SyncAction.NONE
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
                workspace.updateIndexFiles(diskNodes, serverFiles)

                val indexMap = workspace.getIndexFilesFlow().first()
                    .associateBy { it.relativePath.normalizeText() }
                val serverMap = serverFiles.associateBy { it.relativePath.normalizeText() }
                val allPaths = (indexMap.keys + serverMap.keys).distinct()

                val toDownload = mutableMapOf<String, String>()
                val toUpload = mutableMapOf<String, String>()

                allPaths.forEach { path ->
                    val local = indexMap[path]
                    val server = serverMap[path]

                    when (determineAction(local, server)) {
                        SyncAction.DOWNLOAD -> toDownload[path] = path
                        SyncAction.UPLOAD -> toUpload[path] = path
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

    override suspend fun syncronize(workspaceEnv: IWorkspaceEnvironment): ResultWrapper<SyncStatus2, String> {
        emitChannel("init", 1)
        val workspaceAbsolutePath = workspaceEnv.getWorkspace().absolutePath
        val hiddenDirPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
        val importArchivePath = Path(hiddenDirPath, "import.zip").toString()
        val importNode =
            FileTreeNode.File(
                FileName(
                    "import",
                    "zip"
                ),
                "",
                hiddenDirPath
            )
        val exportArchiveNode =
            FileTreeNode.File(
                FileName(
                    "export",
                    "zip"
                ),
                "",
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

                for (path in allPaths) {
                    val local = indexMap[path]
                    val server = serverMap[path]

                    when (determineAction(local, server)) {
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

                // 4. Execution: Zipping and Uploading
                emitChannel("pack to zip", 3)
                if (filesToPackInZip.isNotEmpty()) {
                    filesRepo.packFilesToZip(
                        workspaceAbsolutePath,
                        filesToPackInZip,
                        importArchivePath
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
                    )

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

                cleanupTempFiles(importArchivePath, exportArchiveNode)
                emitChannel("success", 7)
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