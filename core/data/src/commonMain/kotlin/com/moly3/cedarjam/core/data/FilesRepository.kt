package com.moly3.cedarjam.core.data

import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.getOtherFileMeta
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileStructure
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.util.IPathWrapper
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FilesRepository(
    private val filesStorage: ISystemFilesManager
) : IFilesRepository {

    override fun toAbsoluteAppPath(relativePath: IPathWrapper): IPathWrapper {
        return filesStorage.toAbsoluteAppPath(relativePath = relativePath)
    }

    override fun extractZipFromBytes(
        bytes: ByteArray,
        destinationPath: String,
        fileStructure: FileStructure
    ) {
        filesStorage.extractZipFromBytes(bytes, destinationPath, fileStructure)
    }

    override suspend fun extractZip(
        archivePath: String,
        workspaceFullPath: String,
        serverFiles: List<FileItem>
    ) {
        _root_ide_package_.com.moly3.cedarjam.core.storage.func.commonGuy.extractZip(
            archivePath,
            workspaceFullPath,
            serverFiles
        )
    }

    override suspend fun packToZip(
        workspaceFolderAbsolutePath: String,
        filesToArchive: List<String>,
        archivePath: String
    ) {
        _root_ide_package_.com.moly3.cedarjam.core.storage.func.commonGuy.packToZip(
            workspaceFolderAbsolutePath = workspaceFolderAbsolutePath,
            filesToArchive = filesToArchive,
            archivePath = archivePath
        )
    }

    override fun getNodes(node: FileTreeNode.Directory): List<FileTreeNode> {
        val fullPath = node.getFullPath()
        val absolutePath = filesStorage.toAbsoluteAppPath(
            pathWrapper(
                fullPath
            )
        )
        val allFiles = filesStorage.getNodes(absolutePath.pathString)
        val mt =
            getOtherFileMeta(absolutePath.pathString)
        val main =
            listOf(
                node.copy(
                    children = allFiles,
                    fileSize = allFiles.sumOf { x -> x.fileSize },
                    modifiedTime = mt.modifiedDateTime.toEpochMilliseconds()
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

    override fun createNode(
        node: FileTreeNode,
        byteArray: ByteArray?
    ): ResultWrapper<FileTreeNode, String> {
        return filesStorage.createNode(
            isDirectory = node is FileTreeNode.Directory,
            nodePath = node.getFullPath(),
            byteArray = byteArray
        )
    }

    override fun getFileNodeFromFullPath(fullPath: String, isDirectory: Boolean): FileTreeNode {
        return if (isDirectory)
            filesStorage.getDirectoryNodeFromFullPath(
                fullPath = fullPath,
            )
        else
            filesStorage.getFileNodeFromFullPath(
                fullPath = fullPath,
            )
    }

    override fun moveNode(
        node: FileTreeNode,
        newNode: FileTreeNode
    ): ResultWrapper<FileTreeNode, String> {
        return resultBlock {
            bind(
                filesStorage.moveNode(
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
}