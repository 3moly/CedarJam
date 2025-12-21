package com.moly3.cedarjam.core.domain.usecase

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
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
import com.moly3.cedarjam.core.domain.model.isSuccess
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
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
    private val filesRepo: IFilesRepository
) : ISyncUseCase {

    private val _sendingBranch =
        MutableStateFlow<UIState<SyncStatusChannel, String>>(UIState.Loading)

    override fun sendingBranchFlow(): Flow<UIState<SyncStatusChannel, String>> {
        return _sendingBranch.asStateFlow()
    }

    private suspend fun getStatus(
        workspace: IWorkspaceEnvironment,
        localNodes: List<FileTreeNode>,
        serverFiles: List<FileItem>
    ): ResultWrapper<Unit, String> {
        return withContext(io) {
            resultBlock {
                workspace.updateIndexFilesLocal(
                    localNodes = localNodes
                )
                workspace.updateIndexFiles(
                    localNodes = localNodes,
                    serverNodes = serverFiles
                )
            }
        }
    }

    override suspend fun getStatus(workspace: IWorkspaceEnvironment): ResultWrapper<GetSyncStatus, String> {
        return withContext(io) {
            resultBlock {
                val serverFilesResult = workspace.getServerFiles()
                val serverNodes = bind(serverFilesResult).files
                val diskTree1 = workspace.getNodes(null)
                val localNodes1 = diskTree1.firstOrNull()?.getChildrenOrNull()?.getAll() ?: listOf()
                getStatus(
                    workspace,
                    localNodes1,
                    serverFiles = serverNodes
                )
                val serverMap = serverNodes.associateBy { b -> b.relativePath.normalizeText() }
                val indexMap =
                    workspace.getIndexFiles().associateBy { b -> b.relativePath.normalizeText() }
                var toDownload = mutableMapOf<String, String>()
                var toUpload = mutableMapOf<String, String>()
                indexMap.forEach { b ->
                    val localFound = b.value
                    val serverFound = serverMap[b.key]
                    if (serverFound == null) {
                        toUpload[b.key] = b.key
                    } else {
                        if (localFound.isDirectory != serverFound.isDirectory &&
                            localFound.modifiedTime > serverFound.modifiedTime ||
                            localFound.contentHash != serverFound.contentHash &&
                            localFound.modifiedTime > serverFound.modifiedTime ||
                            (localFound.modifiedTime > serverFound.modifiedTime &&
                                    !localFound.isDirectory)
                        ) {
                            toUpload[b.key] = b.key
                        }
                    }
                }
                serverMap.forEach { b ->
                    if (toUpload[b.key] != null) {
                        return@forEach
                    }
                    val serverFound = b.value
                    val localFound = indexMap[b.key]
                    if (localFound == null) {
                        if (!serverFound.isDeleted) {
                            toDownload[b.key] = b.key
                        }
                    } else {
                        if (localFound.isDirectory != serverFound.isDirectory ||
                            localFound.contentHash != serverFound.contentHash ||
                            (localFound.modifiedTime < serverFound.modifiedTime &&
                                    !localFound.isDirectory)
                        ) {
                            toDownload[b.key] = b.key
                        }
                    }
                }

                GetSyncStatus(
                    toDownload = toDownload.toPersistentMap(),
                    toUpload = toUpload.toPersistentMap()
                )
            }
        }
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

    private fun getFilesToDownload(
        dbIndexes: Map<String, IndexFileDto>,
        serverFiles: List<FileItem>
    ): List<String> {
        return serverFiles.mapNotNull { serverNode ->
            if (serverNode.isDeleted) return@mapNotNull null

            val foundNode =
                dbIndexes[serverNode.relativePath] ?: return@mapNotNull serverNode.relativePath

            when {
                foundNode.isDirectory != serverNode.isDirectory -> serverNode.relativePath
                foundNode.isDirectory -> null //already is a directory and exists, so no need to redownload 0 bytes
                foundNode.modifiedTime != serverNode.modifiedTime ||
                        foundNode.size != serverNode.size ||
                        foundNode.contentHash != serverNode.contentHash -> serverNode.relativePath

                else -> null
            }
        }
    }

    private fun processServerDeletions(
        workspace: IWorkspaceEnvironment,
        localNodes: Map<String, FileTreeNode>,
        serverFiles: List<FileItem>
    ): List<String> {
        val abso = workspace.getWorkspace().absolutePath
        val explicitlyDeletedByServer = serverFiles.filter {
            val foundNode = localNodes[it.relativePath]

            it.isDeleted && foundNode != null && foundNode.modifiedTime <= it.modifiedTime
        }

        val deletedPaths = mutableListOf<String>()

        if (explicitlyDeletedByServer.isNotEmpty()) {
            val pathsToDelete = explicitlyDeletedByServer.map { it.relativePath }
            Logger.d { "Server ordered delete for: $pathsToDelete" }

            // 1. Физическое удаление с диска
            // (Предполагаем, что deleteNodesByPath принимает список строк путей)
            val absolutes = pathsToDelete.map { d ->
                pathWrapper(abso, d).pathString
            }
            for (item in absolutes) {

                val meta = SystemFileSystem.metadataOrNull(Path(item)) ?: continue
                val node = filesRepo.getFileNodeFromFullPath(item, isDirectory = meta.isDirectory)
                filesRepo.deleteNodeHeavy(node)
            }
            workspace.deleteIndexFiles(pathsToDelete)

            deletedPaths.addAll(pathsToDelete)
        }

        return deletedPaths
    }

    private suspend fun emitChannel(message: String, progress: Int, fileProgress: Float? = null) {
        _sendingBranch.emit(
            UIState.Success(
                SyncStatusChannel(
                    message,
                    progress = progress,
                    all = 7,
                    fileProgress = fileProgress
                )
            )
        )
    }

    override suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus2, String> {
        emitChannel("init", 1)
        val workspaceAbsolutePath = workspace.getWorkspace().absolutePath
        val hiddenDirPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
        val importArchivePath = FileTreeNode.File(
            name = FileName("import", "zip"),
            parentFullPath = hiddenDirPath,
            parentRelativePath = hiddenDirPath
        ).getFullPath()
        try {
            SystemFileSystem.delete(Path(importArchivePath))
        } catch (e: Exception) {
        }
        val exportArchiveNode = FileTreeNode.File(
            name = FileName("export", "zip"),
            parentRelativePath = hiddenDirPath,
            parentFullPath = hiddenDirPath
        )
        return resultBlock {
            try {
                val serverFilesResult = workspace.getServerFiles()
                val serverFilesAll = bind(serverFilesResult).files

                val diskTree1 = workspace.getNodes(null)
                val localNodes1 = diskTree1.firstOrNull()?.getChildrenOrNull()?.getAll() ?: listOf()
                getStatus(
                    workspace,
                    localNodes1,
                    serverFilesAll
                )
                emitChannel("checked local db", 2)
                val mappedNodes = localNodes1.associateBy { n -> n.getRelativePath() }

                val serverDeletedPaths = processServerDeletions(
                    workspace,
                    localNodes = mappedNodes,
                    serverFilesAll
                )

                val activeServerFiles = serverFilesAll.filter { !it.isDeleted }

                val diskTree = workspace.getNodes(null)
                val localNodes = diskTree.firstOrNull()?.getChildrenOrNull()?.getAll() ?: listOf()

                workspace.updateIndexFiles(
                    localNodes = localNodes,
                    serverNodes = activeServerFiles
                )
                val dbIndexState = workspace.getIndexFilesFlow().first()

                val filesToUploadMeta = mutableListOf<FileMetadata>()
                val editFilesToSync = mutableListOf<IndexFileDto>()
                val filesToPackInZip = mutableListOf<String>()

                for (file in dbIndexState) {
                    val status = file.serverSyncStatus
                    if (status == SyncStatus.SYNCED) continue

                    val meta = file.toMetadata()
                    filesToUploadMeta.add(meta)
                    if ((status == SyncStatus.NEW || status == SyncStatus.DIRTY) &&
                        !meta.isDeleted
                    ) {
                        editFilesToSync.add(file)
                        filesToPackInZip.add(meta.relativePath)
                    }
                }

                emitChannel("pack to zip", 3)
                if (filesToPackInZip.isNotEmpty()) {
                    filesRepo.packFilesToZip(
                        workspaceFolderAbsolutePath = workspace.getWorkspace().absolutePath,
                        filesToArchive = filesToPackInZip,
                        archivePath = importArchivePath
                    )
                }

                val filesToDownloadEstimation = getFilesToDownload(
                    dbIndexState.associateBy { b -> b.relativePath },
                    activeServerFiles
                )
                emitChannel("upload to server", 4)
                val uploadResult = workspace.uploadSync(
                    archiveFullPath = importArchivePath,
                    metadata = filesToUploadMeta.map {
                        val hash = filesRepo.getFileHash(fullPath = pathWrapper(workspaceAbsolutePath,it.relativePath).pathString)
                        it.copy(contentHash = hash)
                    },
                    filesToDownload = filesToDownloadEstimation,
                    onUpload = { one, two ->
                        val progressFile = (if (two != null) {
                            one / two.toFloat()
                        } else 0.0f).coerceIn(0f, 1f)
                        emitChannel("onUpload file", 5, fileProgress = progressFile)
                    },
                    onDownload = { one, two ->
                        val progressFile = (if (two != null) {
                            one / two.toFloat()
                        } else 0.0f).coerceIn(0f, 1f)
                        emitChannel("onDownload file", 6, fileProgress = progressFile)
                    }
                )
                uploadResult.shouldBeSuccess()

                workspace.syncDirtyFiles(editFilesToSync)
                val responseZipBytes = if (uploadResult.isSuccess()) {
                    val myDeletedFilesConfirmed = filesToUploadMeta.filter { it.isDeleted }
                    if (myDeletedFilesConfirmed.isNotEmpty()) {
                        workspace.deleteIndexFiles(myDeletedFilesConfirmed.map { it.relativePath })
                    }
                    bind(uploadResult)
                } else ByteArray(0)

                var extractedFiles = listOf<String>()
                workspace.closeDatabase()
                emitChannel("extract from zip", 6)
                if (responseZipBytes.isNotEmpty()) {
                    filesRepo.deleteNode(exportArchiveNode)
                    filesRepo.createNode(exportArchiveNode, byteArray = responseZipBytes)

                    val exportArchivePath = exportArchiveNode.getFullPath()

                    extractedFiles = filesRepo.unpackZip(
                        archivePath = exportArchivePath,
                        workspaceFullPath = workspaceAbsolutePath,
                    )

                    workspace.setFilesAsSynced(extractedFiles, serverNodes = activeServerFiles)
                        .shouldBeSuccess()
                }

                try {
                    SystemFileSystem.delete(Path(importArchivePath))
                    filesRepo.deleteNode(exportArchiveNode)
                } catch (e: Exception) {
                }
                emitChannel("success", 7)
                SyncStatus2(
                    isLoading = true
                )
            } catch (exc: Exception) {
                Logger.e(exc) { "Sync failed: ${exc.message}" }
                raise(exc.message ?: "Unknown sync error")
            }
        }
    }
}