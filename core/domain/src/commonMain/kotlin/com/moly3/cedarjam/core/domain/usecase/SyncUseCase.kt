package com.moly3.cedarjam.core.domain.usecase

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
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
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.isSuccess
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class SyncUseCase(
    private val filesRepo: IFilesRepository
) : ISyncUseCase {

    private suspend fun packToZip(
        workspacePresentation: WorkspacePresentation,
        filesToArchive: List<String>,
        archivePath: String
    ) {
        filesRepo.packFilesToZip(
            workspaceFolderAbsolutePath = workspacePresentation.absolutePath,
            filesToArchive = filesToArchive,
            archivePath = archivePath
        )
    }

    override suspend fun getStatus(workspace: IWorkspaceEnvironment): ResultWrapper<Unit, String> {
        return withContext(io) {
            resultBlock {
                val tree = workspace.getNodes(null)
                val localNodes = tree.firstOrNull()?.getChildrenOrNull() ?: listOf()
                workspace.updateIndexFilesLocal(
                    localNodes = localNodes
                )
                val serverFilesResult = workspace.getServerFiles()
                val serverNodes = bind(serverFilesResult).files
                workspace.updateIndexFiles(
                    localNodes = localNodes,
                    serverNodes = serverNodes
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
        return serverFiles.filter { serverNode ->
            val foundNode = dbIndexes[serverNode.relativePath]
            if (!serverNode.isDeleted) {
                if (foundNode != null) {
                    if (foundNode.isDirectory != serverNode.isDirectory) {
                        true
                    } else if (foundNode.isDirectory) {
                        false
                    } else if (foundNode.modifiedTime != serverNode.modifiedTime ||
                        foundNode.size != serverNode.size ||
                        foundNode.contentHash != serverNode.contentHash
                    ) {
                        true
                    } else {
                        false
                    }
                } else
                    true
            } else {
                false
            }
        }.map { it.relativePath }
    }

    /**
     * ШАГ 0: Выполняем приказы сервера об удалении.
     * Это решает проблему "файлы не удаляются локально".
     */
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

    override suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus2, String> {
        getStatus(workspace)
        val workspaceAbsolutePath = workspace.getWorkspace().absolutePath
        val hiddenDirPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
        val importArchivePath = FileTreeNode.File(
            name = FileName("import", "zip"),
            parentFullPath = hiddenDirPath,
            parentRelativePath = hiddenDirPath
        ).getFullPath()
        val exportArchiveNode = FileTreeNode.File(
            name = FileName("export", "zip"),
            parentRelativePath = hiddenDirPath,
            parentFullPath = hiddenDirPath
        )
        return resultBlock {
            try {
                try {
                    SystemFileSystem.delete(Path(importArchivePath))
                } catch (e: Exception) { /* ignore */
                }
                val serverFilesResult = workspace.getServerFiles()
                val serverFilesAll = bind(serverFilesResult).files

                val diskTree1 = workspace.getNodes(null)
                val localNodes1 = diskTree1.firstOrNull()?.getChildrenOrNull()?.getAll() ?: listOf()
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

                    // Пропускаем полностью синхронизированные файлы
                    if (status == SyncStatus.SYNCED) continue

                    val meta = file.toMetadata()
                    filesToUploadMeta.add(meta)

                    // Пакуем тело файла, только если это НОВЫЙ или ИЗМЕНЕННЫЙ файл (и не папка, и не удален)
                    if ((status == SyncStatus.NEW || status == SyncStatus.DIRTY) &&
                        !meta.isDeleted
                    ) {
                        editFilesToSync.add(file)
                        filesToPackInZip.add(meta.relativePath)
                    }
                }

                if (filesToPackInZip.isNotEmpty()) {
                    packToZip(
                        workspacePresentation = workspace.getWorkspace(),
                        filesToArchive = filesToPackInZip,
                        archivePath = importArchivePath
                    )
                }
                val filesToDownloadEstimation = getFilesToDownload(
                    dbIndexState.associateBy { b -> b.relativePath },
                    activeServerFiles
                )
                val uploadResult = workspace.uploadSync(
                    filesToDownload = filesToDownloadEstimation,
                    archiveFullPath = importArchivePath,
                    metadata = filesToUploadMeta
                )
                uploadResult.shouldBeSuccess()

                workspace.syncDirtyFiles(editFilesToSync)

                // 10. Обработка ZIP ответа (Скачивание файлов)
                val responseZipBytes = if (uploadResult.isSuccess()) {
                    val myDeletedFilesConfirmed = filesToUploadMeta.filter { it.isDeleted }
                    if (myDeletedFilesConfirmed.isNotEmpty()) {
                        workspace.deleteIndexFiles(myDeletedFilesConfirmed.map { it.relativePath })
                    }
                    bind(uploadResult)
                } else ByteArray(0)

                var extractedFiles = listOf<String>()

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
                SyncStatus2(
                    filesDownloaded = extractedFiles,
                    filesToDownload = filesToDownloadEstimation,
                    localDeletedFilesByServer = listOf(), // Отчет: что удалил сервер
                    filesToArchive = filesToUploadMeta
                )
            } catch (exc: Exception) {
                Logger.e(exc) { "Sync failed: ${exc.message}" }
                raise(exc.message ?: "Unknown sync error")
            }
        }
    }
}