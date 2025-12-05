package com.moly3.cedarjam.core.domain.repository

import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileStructure
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors
import com.moly3.cedarjam.core.domain.util.IPathWrapper

interface IFilesRepository {
    fun toAbsoluteAppPath(relativePath: IPathWrapper): IPathWrapper
    fun extractZipFromBytes(bytes: ByteArray, destinationPath: String, fileStructure: FileStructure)
    suspend fun extractFilesFromZip(
        archivePath: String,
        workspaceFullPath: String,
        serverFiles: List<FileItem>
    )
    suspend fun packFilesToZip(
        workspaceFolderAbsolutePath: String,
        filesToArchive: List<String>,
        archivePath: String,
    )
    fun getFileNodeFromFullPath(fullPath: String, isDirectory: Boolean): FileTreeNode
    fun getNodes(node: FileTreeNode.Directory): List<FileTreeNode>
    fun isNodeExists(node: FileTreeNode): Boolean
    fun deleteNode(node: FileTreeNode)
    fun createNode(
        node: FileTreeNode,
        byteArray: ByteArray? = null
    ): ResultWrapper<FileTreeNode, String>

    fun moveNode(node: FileTreeNode, newNode: FileTreeNode): ResultWrapper<FileTreeNode, String>
    fun setNodeText(node: FileTreeNode.File, text: String): ResultWrapper<Unit, String>
    fun getNodeText(node: FileTreeNode.File): ResultWrapper<String, String>
    fun getNodeBytes(node: FileTreeNode.File): ByteArray
    fun getNodeCanvas(nodePath: String): ResultWrapper<CanvasDataWithErrors, String>
    fun saveNodeCanvas(nodePath: String, data: CanvasDataWithErrors): ResultWrapper<Unit, String>
}