package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.func.getRelativePath
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
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
    ) {
        val deleteFiles = localNodes.filter { localNode ->
            val localRelativePath =
                localNode.getRelativePath(workspacePath = workspaceAbsolutePath)

            serverFiles.firstOrNull { d ->
                d.relativePath == localRelativePath &&
                        d.isDeleted &&
                        d.modifiedTime > localNode.modifiedTime
            } != null
        }
        //delete local file nodes
        for (item in deleteFiles) {
            workspace.deleteNode(item)
        }
    }

    private suspend fun step3(
        workspacePresentation: WorkspacePresentation,
        filesToArchive: List<String>,
        archivePath: String
    ) {
//        zipFolder(
//            workspaceFolderAbsolutePath = workspacePresentation.absolutePath,
//            filesToArchive = filesToArchive,
//            archive = archivePath
//        )
        filesRepo.packToZip(
            workspaceFolderAbsolutePath = workspacePresentation.absolutePath,
            filesToArchive = filesToArchive,
            archivePath = archivePath
        )
    }

    private fun step4(
        workspace: IWorkspaceEnvironment,
        filesToArchive: List<FileMetadata>
    ): List<FileMetadata> {
        val metadataList = filesToArchive.toMutableList()
        val deleted = workspace.getDeletedFilesMetadata(workspace = workspace)
        for (deletedMeta in deleted) {
            val upload = metadataList.firstOrNull { d -> d.path == deletedMeta.key }
            if (upload != null && deletedMeta.value > upload.modifiedTime || upload == null) {
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
            if (!serverNode.isDeleted) {
                val localNode =
                    allMetadata.firstOrNull { m -> m.path == serverNode.relativePath && !m.isDeleted }
                if (localNode != null) {
                    localNode.modifiedTime < serverNode.modifiedTime
                } else
                    true
            } else
                false
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
                    if (meta.name.extension == "db-shm" ||
                        meta.name.extension == "db-wal" ||
                        meta.name.name == "deleted_files" ||
                        meta.name.extension == "db-journal"
                    ) {

                    } else {
                        metadataList.add(
                            FileMetadata(
                                path = meta.getRelativePath(workspacePath = workspacePresentation.absolutePath),
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
            if (foundServerFile == null || foundServerFile.modifiedTime < localFile.modifiedTime) {
                addMeta(localFile)
            }
        }
        return metadataList
    }


    override suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<Unit, String> {
        return resultBlock {
            try {
                val workspaceAbsolutePath = workspace.getWorkspace().absolutePath
                val localStructure = workspace.getNodes(null)
                val localNodes = localStructure.getAll(isSkipOwnNode = true)

                val serverNodes = step1(workspace = workspace)
                step2(
                    workspaceAbsolutePath = workspaceAbsolutePath,
                    localNodes = localNodes,
                    serverFiles = serverNodes,
                    workspace = workspace
                )
                val archivePath = filesRepo.toAbsoluteAppPath(pathWrapper("meme.zip")).pathString
//                val file = filesRepo.getFileNodeFromFullPath(archivePath, isDirectory = false)
//                filesRepo.deleteNode(file)
                try {
                    SystemFileSystem.delete(Path(archivePath))
                } catch (exc: Exception) {
                }

                val filesToArchive = getFilesToArchive(
                    workspacePresentation = workspace.getWorkspace(),
                    localNodes = localNodes,
                    serverNodes = serverNodes
                )
                step3(
                    workspacePresentation = workspace.getWorkspace(),
                    filesToArchive = filesToArchive.map { d -> d.path },
                    archivePath = archivePath
                )
                val metadata = step4(
                    workspace = workspace,
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
                    metadata = metadata
                )
                uploadResult.shouldBeSuccess()

                //                serverFilesResult.shouldBeSuccess()
                val serverFiles2 = when (val serverFilesResult2 = workspace.getServerFiles()) {
                    is ResultWrapper.Error -> {
                        //todo check by server error
                        listOf()
                    }

                    is ResultWrapper.Success -> {
                        serverFilesResult2.value.files
                    }
                }

                val filesDirPath = filesRepo.toAbsoluteAppPath(pathWrapper("")).pathString
                val exportArchiveNode =
                    FileTreeNode.File(
                        name = FileName(
                            "meme_export",
                            extension = "zip"
                        ),
                        parentPath = filesDirPath
                    )
                val exportArchivePath = exportArchiveNode.getFullPath()
                filesRepo.deleteNode(exportArchiveNode)
                val createdExportArchive =
                    filesRepo.createNode(exportArchiveNode, byteArray = uploadResult.value)
                createdExportArchive.shouldBeSuccess()
                val dbWall =
                    FileTreeNode.File(
                        name = FileName(
                            "sqlite",
                            extension = "db-wal"
                        ),
                        parentPath = pathWrapper(filesDirPath, hiddenDirectory).pathString
                    )
                val dbShm =
                    FileTreeNode.File(
                        name = FileName(
                            "sqlite",
                            extension = "db-shm"
                        ),
                        parentPath = pathWrapper(filesDirPath, hiddenDirectory).pathString
                    )
                try {
                    filesRepo.deleteNode(dbWall)
                    filesRepo.deleteNode(dbShm)
                } catch (exc: Exception) {

                }
                //filesRepo.extractZipFromBytes()
                filesRepo.extractZip(
                    archivePath = exportArchivePath,
                    workspaceFullPath = workspaceAbsolutePath,
                    serverFiles = serverFiles2,
                )
//                extractZipFromBytes(
//                    bytes = uploadResult.value,
//                    destinationPath = workspaceAbsolutePath,
//                    fileStructure = FileStructure(modifiedTime = 0L, files = serverFiles2)
//                )

            } catch (exc: Exception) {
                raise(exc.message ?: "")
            }
        }
//        return either {
//
//        }.toResult()
    }
}