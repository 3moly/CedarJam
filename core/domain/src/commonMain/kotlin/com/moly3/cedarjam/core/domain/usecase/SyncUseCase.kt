package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.func.getRelativePath
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.isMoreThan
import com.moly3.cedarjam.core.domain.func.isMoreThanOrExact
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.collections.iterator

class SyncUseCase(
    private val filesRepo: IFilesRepository
) : ISyncUseCase {

    private suspend fun step1(workspace: IWorkspaceEnvironment): List<FileItem> {
        return when (val serverFilesResult = workspace.getServerFiles()) {
            is ResultWrapper.Error -> {
                //todo check by server error
                listOf()
            }

            is ResultWrapper.Success -> {
                serverFilesResult.value.files
            }
        }
    }

    //step 1: delete local files
    private suspend fun step2(
        workspaceAbsolutePath: String,
        localNodes: List<FileTreeNode>,
        serverFiles: List<FileItem>,
        workspace: IWorkspaceEnvironment
    ): List<FileTreeNode> {
        val deleteFiles = localNodes.filter { localNode ->
            val localRelativePath =
                localNode.getRelativePath(workspacePath = workspaceAbsolutePath)

            serverFiles.firstOrNull { d ->
                d.relativePath.normalizeText() == localRelativePath.normalizeText() &&
                        d.isDeleted &&
                        d.modifiedTime.isMoreThan(localNode.modifiedTime)
            } != null
        }
        workspace.deleteNodes(deleteFiles)
        return deleteFiles
    }

    private suspend fun step3(
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

    private fun step4(
        localDeletedFiles: Map<String, Long>,
        filesToArchive: List<FileMetadata>
    ): List<FileMetadata> {
        val metadataList = filesToArchive.toMutableList()
        for (deletedMeta in localDeletedFiles) {
            val upload =
                metadataList.firstOrNull { d -> d.path.normalizeText() == deletedMeta.key.normalizeText() }
            if (upload != null && deletedMeta.value.isMoreThan(upload.modifiedTime) || upload == null) {
                metadataList.remove(upload)
                metadataList.add(
                    FileMetadata(
                        path = deletedMeta.key,
                        createdTime = deletedMeta.value,
                        modifiedTime = deletedMeta.value,
                        isDeleted = true
                    )
                )
            }
        }
        return metadataList
    }

    private fun step5(
        allMetadata: List<FileMetadata>,
        serverNodes: List<FileItem>
    ): List<String> {
        return serverNodes.filter { serverNode ->
            if (serverNode.isDirectory)
                false
            else {
                if (!serverNode.isDeleted) {
                    val localNode =
                        allMetadata.firstOrNull { m ->
                            m.path == serverNode.relativePath.normalizeText()
                        } //&& !m.isDeleted
                    if (localNode != null) {
                        if (localNode.isDeleted &&
                            localNode.modifiedTime.isMoreThanOrExact(serverNode.modifiedTime)
                        ) false
                        else {
                            val result = serverNode.modifiedTime.isMoreThan(localNode.modifiedTime)
                            result
                        }
                    } else
                        true
                } else
                    false
            }
        }.map { v -> v.relativePath }
    }

    private fun getFilesToArchive(
        workspacePresentation: WorkspacePresentation,
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): List<FileMetadata> {
        val metadataList = mutableListOf<FileMetadata>()
        fun addMeta(meta: FileTreeNode) {
            when (meta) {
                is FileTreeNode.Directory -> {

                }

                is FileTreeNode.File -> {
                    val relativePath = meta.getRelativePath(workspacePath = workspacePresentation.absolutePath)
                    if (meta.name.extension == "db-shm" ||
                        meta.name.extension == "db-wal" ||
                        meta.name.name == "deleted_files" ||
                        meta.name.extension == "db-journal" ||
                        relativePath == "${hiddenDirectory}/import.zip" ||
                        relativePath == "${hiddenDirectory}/export.zip"
                    ) {

                    } else {
                        metadataList.add(
                            FileMetadata(
                                path = relativePath,
                                createdTime = meta.createdTime,
                                modifiedTime = meta.modifiedTime,
                                isDeleted = false
                            )
                        )
                    }
                }
            }
        }
        for (localFile in localNodes) {
            val localRelativePath =
                localFile.getRelativePath(workspacePath = workspacePresentation.absolutePath)
            val foundServerFile =
                serverNodes.firstOrNull { d -> d.relativePath == localRelativePath }
            if (foundServerFile == null || localFile.modifiedTime.isMoreThan(foundServerFile.modifiedTime)) {
                addMeta(localFile)
            }
        }
        return metadataList
    }


    override suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus, String> {
        val workspaceAbsolutePath = workspace.getWorkspace().absolutePath

        val archivePath =
            FileTreeNode.File(
                name = FileName(
                    "import",
                    extension = "zip"
                ),
                parentPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
            ).getFullPath()
        val dbWall =
            FileTreeNode.File(
                name = FileName(
                    "sqlite",
                    extension = "db-wal"
                ),
                parentPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
            )
        val dbShm =
            FileTreeNode.File(
                name = FileName(
                    "sqlite",
                    extension = "db-shm"
                ),
                parentPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
            )
        val exportArchiveNode =
            FileTreeNode.File(
                name = FileName(
                    "export",
                    extension = "zip"
                ),
                parentPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
            )
        return resultBlock {
            try {

                val localNodes = workspace.getNodes(null).getAll(isSkipOwnNode = true)

                //get server file structure
                val serverNodes = step1(workspace = workspace)
                //delete local files
                val deletedLocalFiles = step2(
                    workspaceAbsolutePath = workspaceAbsolutePath,
                    localNodes = localNodes,
                    serverFiles = serverNodes.map { d -> d.copy(relativePath = d.relativePath.normalizeText()) },
                    workspace = workspace
                )

                try {
                    SystemFileSystem.delete(Path(archivePath))
                } catch (exc: Exception) {
                }

                val filesToArchive = getFilesToArchive(
                    workspacePresentation = workspace.getWorkspace(),
                    localNodes = localNodes,
                    serverNodes = serverNodes.map { d -> d.copy(relativePath = d.relativePath.normalizeText()) }
                )
                //pack filesToArchive to zip
                step3(
                    workspacePresentation = workspace.getWorkspace(),
                    filesToArchive = filesToArchive.map { d -> d.path },
                    archivePath = archivePath
                )
                val localDeletedFiles = workspace.getDeletedFilesMetadata().toMutableMap()
                val metadata = step4(
                    localDeletedFiles = localDeletedFiles,
                    filesToArchive = filesToArchive
                )
                val metadatasd = metadata.toMutableList()
                for (localNode in localNodes) {
                    val relativePath =
                        localNode.getRelativePath(workspacePath = workspaceAbsolutePath)
                    val isFound =
                        metadatasd.firstOrNull { b -> relativePath == b.path }
                    if (isFound == null) {
                        metadatasd.add(
                            FileMetadata(
                                path = relativePath,
                                createdTime = localNode.createdTime,
                                modifiedTime = localNode.modifiedTime,
                                isDeleted = false
                            )
                        )
                    }
                }
                val filesToDownload = step5(
                    allMetadata = metadatasd,
                    serverNodes = serverNodes
                )

                val uploadResult = workspace.uploadSync(
                    filesToDownload = filesToDownload,
                    archiveFullPath = archivePath,
                    metadata = metadatasd
                )
                uploadResult.shouldBeSuccess()

                val exportArchivePath = exportArchiveNode.getFullPath()
                filesRepo.deleteNode(exportArchiveNode)
                val createdExportArchive =
                    filesRepo.createNode(exportArchiveNode, byteArray = uploadResult.value)
                createdExportArchive.shouldBeSuccess()

                try {
                    filesRepo.deleteNode(dbWall)
                    filesRepo.deleteNode(dbShm)
                } catch (exc: Exception) {

                }

                val serverFiles2 = step1(workspace)
                var extractedFiles = listOf<String>()
                try {
                    extractedFiles = filesRepo.extractFilesFromZip(
                        archivePath = exportArchivePath,
                        workspaceFullPath = workspaceAbsolutePath,
                        serverFiles = serverFiles2,
                    )
                } catch (exc: Exception) {
                }
                if (localDeletedFiles.isNotEmpty() &&
                    extractedFiles.isNotEmpty()
                ) {
                    var someDeleted = false
                    for (extractFile in extractedFiles) {
                        if (localDeletedFiles.remove(extractFile) != null) {
                            someDeleted = true
                        }
                    }
                    if (someDeleted) {
                        workspace.saveDeletedMetadata(localDeletedFiles)
                    }
                }
                SyncStatus(
                    filesDownloaded = extractedFiles,
                    filesToDownload = filesToDownload,
                    localDeletedFilesByServer = deletedLocalFiles,
                    filesToArchive = filesToArchive
                )
            } catch (exc: Exception) {
                raise(exc.message ?: "")
            }
        }
    }
}