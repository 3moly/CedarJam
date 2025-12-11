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
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import kotlinx.coroutines.flow.first
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

    override suspend fun getStatus(workspace: IWorkspaceEnvironment) {
        resultBlock {
            try {
                val tree = workspace.getNodes(null)
                val serverNodes = step1(workspace = workspace)
                workspace.updateIndexFilesFlow(
                    localNodes = tree.firstOrNull()?.getChildrenOrNull() ?: listOf(),
                    serverNodes = serverNodes
                )
            } catch (exc: Exception) {
                raise(exc.message ?: "")
            }
        }
    }

    private fun IndexFileDto.toMetadata(): FileMetadata {
        return FileMetadata(
            relativePath = this.relativePath,
            modifiedTime = this.modifiedTime,
            contentHash = this.contentHash ?: "",
            isDeleted = this.serverSyncStatus == SyncStatus.DELETED,
            isDirectory = this.isDirectory == 1L
        )
    }

    override suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus2, String> {
        val workspaceAbsolutePath = workspace.getWorkspace().absolutePath
        val hiddenDirPath = pathWrapper(workspaceAbsolutePath, hiddenDirectory).pathString

        // Temp file for upload
        val importArchivePath = FileTreeNode.File(
            name = FileName("import", "zip"),
            parentFullPath = hiddenDirPath,
            parentRelativePath = hiddenDirPath
        ).getFullPath()

        // Temp file for download (server response)
        val exportArchiveNode = FileTreeNode.File(
            name = FileName("export", "zip"),
            parentRelativePath = hiddenDirPath,
            parentFullPath = hiddenDirPath
        )

        return resultBlock {
            try {
                // 0. Cleanup temp files
                try {
                    SystemFileSystem.delete(Path(importArchivePath))
                } catch (e: Exception) { /* ignore */ }

                // --- PHASE 1: DISCOVERY & RECONCILIATION ---

                // 1. Get Real State (Disk)
                // Flatten the tree immediately to list of nodes
                val diskTree = workspace.getNodes(null)
                val localNodes = diskTree.firstOrNull()?.getChildrenOrNull()?.getAll() ?: listOf()

                // 2. Get Server State (API)
                val serverFiles = step1(workspace)

                // 3. Update Local DB Index (The Core Logic)
                // This marks files as NEW, DIRTY, DELETED or SYNCED based on comparison
                workspace.updateIndexFilesFlow(
                    localNodes = localNodes,
                    serverNodes = serverFiles
                )

                // --- PHASE 2: GENERATE SYNC PLAN ---

                // 4. Query DB to find what changed locally
                // You need to add `getIndexedFiles()` to IWorkspaceEnvironment that performs `SELECT * FROM IndexFile`
                val dbIndexState = workspace.getIndexFilesFlow().first()

                val filesToUploadMeta = mutableListOf<FileMetadata>()
                val filesToPackInZip = mutableListOf<String>()

                for (file in dbIndexState) {
                    val status = file.serverSyncStatus

                    // Filter: We only care about things that are NOT SYNCED
                    if (status == SyncStatus.SYNCED) continue

                    // Add to Metadata List (JSON)
                    val meta = file.toMetadata()
                    filesToUploadMeta.add(meta)

                    // Decide if we need to send the physical body
                    // We send body ONLY for NEW or DIRTY files (and not directories)
                    if ((status == SyncStatus.NEW || status == SyncStatus.DIRTY) &&
                        !meta.isDeleted &&
                        !meta.isDirectory
                    ) {
                        filesToPackInZip.add(meta.relativePath)
                    }
                }

                // --- PHASE 3: NETWORK EXECUTION ---

                // 5. Pack ZIP (Only modified content)
                if (filesToPackInZip.isNotEmpty() || true) {
                    packToZip(
                        workspacePresentation = workspace.getWorkspace(),
                        filesToArchive = filesToPackInZip,
                        archivePath = importArchivePath
                    )
                }

                // 6. Upload
                // filesToDownload here is just a hint for UI, the server decides the real diff
                val filesToDownloadEstimation = getFilesToDownload(localNodes, serverFiles)

                val uploadResult = workspace.uploadSync(
                    filesToDownload = filesToDownloadEstimation,
//                    archiveFullPath = if (filesToPackInZip.isNotEmpty()) importArchivePath else "",
                    archiveFullPath = importArchivePath,
                    metadata = filesToUploadMeta
                )
                uploadResult.shouldBeSuccess()

                // --- PHASE 4: APPLY SERVER RESPONSE ---

                val responseZipBytes = uploadResult.value
                var extractedFiles = listOf<String>()

                if (responseZipBytes.isNotEmpty()) {
                    // 7. Save and Extract Response ZIP
                    filesRepo.deleteNode(exportArchiveNode)
                    filesRepo.createNode(exportArchiveNode, byteArray = responseZipBytes)

                    val exportArchivePath = exportArchiveNode.getFullPath()

                    try {
                        // Extracts files and Overwrites local ones
                        // CRITICAL: extractFilesFromZip must set the LastModifiedTime from the zip entry!
                        extractedFiles = filesRepo.extractFilesFromZip(
                            archivePath = exportArchivePath,
                            workspaceFullPath = workspaceAbsolutePath,
                            serverFiles = serverFiles // Pass server files to verify hashes if needed
                        )
                        Logger.d { "Extracted ${extractedFiles.size} files from server" }

                        // 8. FINALIZATION: Mark downloaded files as SYNCED in DB
                        // If we don't do this, next run will think we modified them locally.
                        workspace.setFilesAsSynced(extractedFiles, serverNodes = serverFiles)

                    } catch (e: Exception) {
                        Logger.e(e) { "Failed to extract server response" }
                    }
                }

                // 9. Cleanup
                try {
                    SystemFileSystem.delete(Path(importArchivePath))
                    filesRepo.deleteNode(exportArchiveNode)
                } catch (e: Exception) { }

                workspace.finishIndexFiles()
                SyncStatus2(
                    filesDownloaded = extractedFiles,
                    filesToDownload = filesToDownloadEstimation,
                    localDeletedFilesByServer = listOf(), // Handled by updateIndexFilesFlow logic implicitly
                    filesToArchive = filesToUploadMeta
                )
            } catch (exc: Exception) {
                Logger.e(exc) { "Sync failed ${exc.message}" }
                raise(exc.message ?: "Unknown sync error")
            }
        }
    }
}