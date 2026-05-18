package com.moly3.cedarjam.core.data

import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.getOtherFileMeta
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.util.IPathWrapper
import com.moly3.cedarjam.core.storage.func.commonGuy.extractZip
import com.moly3.cedarjam.core.storage.func.commonGuy.packToZip
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FilesRepository(
    private val filesStorage: ISystemFilesManager
) : IFilesRepository {

    override fun toAbsoluteAppPath(relativePath: IPathWrapper): IPathWrapper {
        return filesStorage.toAbsoluteAppPath(relativePath = relativePath)
    }

    override suspend fun unpackZip(
        serverIndexes: List<FileItem>,
        archivePath: String,
        workspaceFullPath: String,
    ): List<String> {
        return extractZip(
            serverIndexes,
            archivePath,
            workspaceFullPath,
        )
    }

    override suspend fun packFilesToZip(
        workspaceFolderAbsolutePath: String,
        filesToArchive: List<String>,
        archivePath: String
    ) {
        packToZip(
            workspaceFolderAbsolutePath = workspaceFolderAbsolutePath,
            filesToArchive = filesToArchive,
            archivePath = archivePath
        )
    }

    override fun getNodes(workspacePath: String, absolutePath: String): List<FileTreeNode> {
        val allFiles = filesStorage.getNodes(absolutePath)
        val mt = getOtherFileMeta(absolutePath)
        val parentRelativePath = if (absolutePath.contains("$workspacePath/")) {
            absolutePath.replace("$workspacePath/", "")
        } else {
            absolutePath.replace(workspacePath, "")
        }
        val main = listOf(
            FileTreeNode.Directory(
                workspaceFullPath = workspacePath,
                children = allFiles,
                name = "",
                fileSize = allFiles.sumOf { x -> x.fileSize },
                modifiedTime = mt.modifiedDateTime.toEpochMilliseconds(),
                parentRelativePath = parentRelativePath,
                createdTime = mt.createdDateTime.toEpochMilliseconds(),
            )
        )
        return main
    }

    override fun isNodeExists(node: FileTreeNode): Boolean {
        return filesStorage.isNodeExists(node.getFullPath())
    }

    override fun deleteNode(node: FileTreeNode) {
        filesStorage.deleteNode(node.getFullPath())
    }

    override fun deleteNodeHeavy(node: FileTreeNode) {
        filesStorage.deleteNodeHeavy(node.getFullPath())
    }


    override fun createNode(
        workspacePath: String,
        node: FileTreeNode,
        byteArray: ByteArray?
    ): ResultWrapper<FileTreeNode, String> {
        return filesStorage.createNode(
            workspacePath = workspacePath,
            isDirectory = node is FileTreeNode.Directory,
            nodePath = node.getFullPath(),
            byteArray = byteArray,
            isMustCreate = true
        )
    }

    override fun createDirectory(
        workspacePath: String,
        fullPath: String,
        isMustCreate: Boolean
    ): ResultWrapper<Unit, String> {
        return resultBlock {
            filesStorage.createNode(
                workspacePath = workspacePath,
                isDirectory = true,
                nodePath = fullPath,
                byteArray = null,
                isMustCreate = isMustCreate
            )
        }
    }

    override fun getFileNodeFromFullPath(
        workspacePath: String,
        fullPath: String,
        isDirectory: Boolean
    ): FileTreeNode {
        return if (isDirectory)
            filesStorage.getDirectoryNodeFromFullPath(
                workspacePath = workspacePath,
                fullPath = fullPath,
            )
        else
            filesStorage.getFileNodeFromFullPath(
                workspacePath = workspacePath,
                fullPath = fullPath,
            )
    }

    override fun moveNode(
        workspacePath: String,
        node: FileTreeNode,
        newNode: FileTreeNode
    ): ResultWrapper<FileTreeNode, String> {
        return resultBlock {
            bind(
                filesStorage.moveNode(
                    workspacePath = workspacePath,
                    nodePath = node.getFullPath(),
                    moveNodePath = newNode.getFullPath(),
                    isDirectory = newNode is FileTreeNode.Directory
                )
            )
        }
    }

    override fun setNodeText(node: FileTreeNode.File, text: String): ResultWrapper<Unit, String> {
        return filesStorage.setNodeText(node.getFullPath(), text = text)
    }

    override fun getNodeText(node: FileTreeNode.File): ResultWrapper<String, String> {
        return resultBlock {
            try {
                filesStorage.getNodeText(node.getFullPath())
            } catch (exc: Exception) {
                raise(exc.message ?: "error getNodeText")
            }
        }
    }

    override fun setNodeBytes(node: FileTreeNode.File, byteArray: ByteArray) {
        filesStorage.setNodeBytes(nodePath = node.getFullPath(), byteArray = byteArray)
    }

    override fun getNodeBytes(node: FileTreeNode.File): ByteArray {
        return filesStorage.getNodeBytes(node.getFullPath())
    }

    override fun getNodeCanvas(nodePath: String): ResultWrapper<CanvasDataWithErrors, String> {
        return filesStorage.getNodeCanvas(nodePath = nodePath)
    }

    override fun saveNodeCanvas(
        nodePath: String,
        data: CanvasDataWithErrors
    ): ResultWrapper<Unit, String> {
        return filesStorage.saveNodeCanvas(nodePath = nodePath, data = data)
    }

    override fun getFileHash(fullPath: String): String {
        return filesStorage.getFileHash(fullPath)
    }
}