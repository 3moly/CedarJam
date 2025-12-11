package com.moly3.cedarjam.core.domain.usecase

import co.touchlab.kermit.Logger
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
    private fun getWhatToDeleteInLocal(
        localNodes: List<FileTreeNode>,
        serverFiles: List<FileItem>
    ): List<FileTreeNode> {
        val deleteFiles = localNodes.filter { localNode ->
            val localRelativePath = localNode.getRelativePath().normalizeText()
            serverFiles.firstOrNull { serverNode ->
                serverNode.relativePath.normalizeText() == localRelativePath &&
                        serverNode.isDeleted &&
                        serverNode.modifiedTime.isMoreThan(localNode.modifiedTime)
            } != null
        }
        return deleteFiles
    }

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

//    private fun step4(
//        localDeletedFiles: Map<String, Long>,
//        filesToArchive: List<FileMetadata>
//    ): List<FileMetadata> {
//        val metadataList = filesToArchive.toMutableList()
//        for (deletedMeta in localDeletedFiles) {
//            val upload =
//                metadataList.firstOrNull { d -> d.relativePath.normalizeText() == deletedMeta.key.normalizeText() }
//            // Fixed: Proper condition with correct precedence
//            if (upload == null || deletedMeta.value.isMoreThan(upload.modifiedTime)) {
//                if (upload != null) {
//                    metadataList.remove(upload)
//                }
//                metadataList.add(
//                    FileMetadata(
//                        relativePath = deletedMeta.key,
//                        createdTime = deletedMeta.value,
//                        modifiedTime = deletedMeta.value,
//                        isDeleted = true
//                    )
//                )
//            }
//        }
//        return metadataList
//    }

//    private fun getAllMetadata(
//        localDeletedFiles: Map<String, Long>,
//        filesToArchive: List<FileMetadata>,
//        localNodes: List<FileTreeNode>,
//    ): List<FileMetadata> {
//        val metadata = step4(
//            localDeletedFiles = localDeletedFiles,
//            filesToArchive = filesToArchive
//        )
//        val metadatasd = metadata.toMutableList()
//        for (localNode in localNodes) {
//            val relativePath = localNode.getRelativePath()
//            val isFound =
//                metadatasd.firstOrNull { b -> relativePath.normalizeText() == b.relativePath.normalizeText() }
//            // Fixed: Proper condition with parentheses and remove old entry before adding new one
//            if (isFound == null || (isFound.isDeleted &&
//                        localNode.modifiedTime.isMoreThanOrExact(isFound.modifiedTime))
//            ) {
//                if (isFound != null) {
//                    metadatasd.remove(isFound)
//                }
//                metadatasd.add(
//                    FileMetadata(
//                        relativePath = relativePath,
//                        createdTime = localNode.createdTime,
//                        modifiedTime = localNode.modifiedTime,
//                        isDeleted = false
//                    )
//                )
//            }
//        }
//        return metadatasd
//    }

    private fun getFilesToDownload(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): List<String> {
        return serverNodes.filter { serverNode ->
            if (serverNode.isDirectory)
                false
            else {
                if (!serverNode.isDeleted) {
                    // Fixed: Normalize both sides of comparison

                    val localNode =
                        localNodes.firstOrNull { m ->
                            m.getRelativePath()
                                .normalizeText() == serverNode.relativePath.normalizeText()
                        }
                    if (localNode != null) {
                        if (serverNode.modifiedTime.isMoreThan(localNode.modifiedTime)) {
                            true
                        } else {
                            false
                        }
                        // Fixed: Removed redundant check since we filter !m.isDeleted above
//                        val result = serverNode.modifiedTime.isMoreThan(localNode.modifiedTime)
//                        result
                    } else
                        true
                } else
                    false
            }
        }.map { v -> v.relativePath }
    }

    private fun getFilesToArchive(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): List<FileMetadata> {
        val metadataList = mutableListOf<FileMetadata>()
        fun addMeta(meta: FileTreeNode, sha256: String? = null) {
            when (meta) {
                is FileTreeNode.Directory -> {}
                is FileTreeNode.File -> {
                    metadataList.add(
                        FileMetadata(
                            relativePath = meta.getRelativePath(),
                            modifiedTime = meta.modifiedTime,
                            contentHash = sha256 ?: filesRepo.getFileHash(meta.getFullPath()),
                            isDirectory = false,
                            isDeleted = false
                        )
                    )
                }
            }
        }
        for (localFile in localNodes) {
            val localRelativePath = localFile.getRelativePath().normalizeText()
            val foundServerFile =
                serverNodes.firstOrNull { d -> d.relativePath.normalizeText() == localRelativePath }
            if (foundServerFile == null) {
                addMeta(localFile)
            } else {
                if (localFile.modifiedTime.isMoreThan(foundServerFile.modifiedTime)) {
                    addMeta(localFile)
                } else {
                    val localSha256 = filesRepo.getFileHash(localFile.getFullPath())
                    if (localSha256 != foundServerFile.contentHash) {
                        addMeta(localFile, localSha256)
                    }
                }
            }
        }
        return metadataList
    }

    override suspend fun getStatus(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus, String> {
        return resultBlock {
            try {
                val localNodes = workspace.getNodes(null).getAll(isSkipOwnNode = true)
                val serverNodes = step1(workspace = workspace)

                val deletedLocalFiles = getWhatToDeleteInLocal(
                    localNodes = localNodes,
                    serverFiles = serverNodes
                )
                val filesToArchive = getFilesToArchive(
                    localNodes = localNodes,
                    serverNodes = serverNodes
                )
                val localDeletedFiles = workspace.getDeletedFilesMetadata().toMutableMap()

//                val allMetadata = getAllMetadata(
//                    localDeletedFiles = localDeletedFiles,
//                    filesToArchive = filesToArchive,
//                    localNodes = localNodes
//                )
                val filesToDownload = getFilesToDownload(
                    localNodes = localNodes,
                    serverNodes = serverNodes
                )
                SyncStatus(
                    filesDownloaded = listOf(),
                    filesToDownload = filesToDownload,
                    localDeletedFilesByServer = deletedLocalFiles,
                    filesToArchive = filesToArchive
                )
            } catch (exc: Exception) {
                raise(exc.message ?: "")
            }
        }
    }

    override suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus, String> {
        val workspaceAbsolutePath = workspace.getWorkspace().absolutePath

        val archivePath =
            FileTreeNode.File(
                name = FileName(
                    "import",
                    extension = "zip"
                ),
                //todo adapt relativePath
                parentFullPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString,
                parentRelativePath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
            ).getFullPath()
        val exportArchiveNode =
            FileTreeNode.File(
                name = FileName(
                    "export",
                    extension = "zip"
                ),
                //todo adapt relativePath
                parentRelativePath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString,
                parentFullPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString
            )
        return resultBlock {
            try {
                try {
                    SystemFileSystem.delete(Path(archivePath))
                } catch (exc: Exception) {
                    // Ignore deletion errors
                }

                val localNodes = workspace.getNodes(null).getAll(isSkipOwnNode = true)
                val serverNodes = step1(workspace = workspace)
                //delete local files
                val deletedLocalFiles = getWhatToDeleteInLocal(
                    localNodes = localNodes,
                    serverFiles = serverNodes
                )
                workspace.deleteNodes(deletedLocalFiles)

                val localMetadata = getFilesToArchive(
                    localNodes = localNodes,
                    serverNodes = serverNodes
                ).toMutableList()
                packToZip(
                    workspacePresentation = workspace.getWorkspace(),
                    filesToArchive = localMetadata.map { d -> d.relativePath },
                    archivePath = archivePath
                )

                val localDeletedFiles = workspace.getDeletedFilesMetadata()
                    .filter { d ->
                        val localFound =
                            localNodes.firstOrNull { b ->
                                b.getRelativePath().normalizeText() == d.key.normalizeText() &&
                                        b.modifiedTime.isMoreThanOrExact(d.value)
                            }
                        val serverFound =
                            serverNodes.firstOrNull { b ->
                                b.relativePath.normalizeText() == d.key.normalizeText() &&
                                        b.modifiedTime.isMoreThanOrExact(d.value)
                            }

                        localFound == null && serverFound == null
                    }
                    .toMutableMap()
                for (item in localDeletedFiles) {
                    localMetadata.add(
                        FileMetadata(
                            relativePath = item.key,
                            modifiedTime = item.value,
                            contentHash = "",
                            isDirectory = false,
                            isDeleted = true
                        )
                    )
                }
                localMetadata
//                val allMetadata = getAllMetadata(
//                    localDeletedFiles = localDeletedFiles,
//                    filesToArchive = filesToArchive,
//                    localNodes = localNodes
//                )
                val filesToDownload = getFilesToDownload(
                    localNodes = localNodes,
                    serverNodes = serverNodes
                )

                val uploadResult = workspace.uploadSync(
                    filesToDownload = filesToDownload,
                    archiveFullPath = archivePath,
                    metadata = localMetadata
                )
                uploadResult.shouldBeSuccess()

                val exportArchivePath = exportArchiveNode.getFullPath()
                filesRepo.deleteNode(exportArchiveNode)
                val createdExportArchive =
                    filesRepo.createNode(exportArchiveNode, byteArray = uploadResult.value)
                createdExportArchive.shouldBeSuccess()

                val serverFiles2 = step1(workspace)
                var extractedFiles = listOf<String>()
                try {
                    extractedFiles = filesRepo.extractFilesFromZip(
                        archivePath = exportArchivePath,
                        workspaceFullPath = workspaceAbsolutePath,
                        serverFiles = serverFiles2
                    )
                    Logger.w { "exctracted files: ${extractedFiles.size}" }
                } catch (exc: Exception) {
                    // Ignore extraction errors
                }
//                if (localDeletedFiles.isNotEmpty() &&
//                    extractedFiles.isNotEmpty()
//                ) {
//                    var someDeleted = false
//                    for (extractFile in extractedFiles) {
//                        if (localDeletedFiles.remove(extractFile) != null) {
//                            someDeleted = true
//                        }
//                    }
//                    if (someDeleted) {
//                        workspace.saveDeletedMetadata(localDeletedFiles)
//                    }
//                }
                SyncStatus(
                    filesDownloaded = listOf(),
                    filesToDownload = filesToDownload,
                    localDeletedFilesByServer = deletedLocalFiles,
                    filesToArchive = localMetadata
                )
            } catch (exc: Exception) {
                raise(exc.message ?: "")
            }
        }
    }
}