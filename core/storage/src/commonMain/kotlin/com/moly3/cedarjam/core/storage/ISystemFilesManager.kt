package com.moly3.cedarjam.core.storage

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.util.IPathWrapper
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors

interface ISystemFilesManager {
    fun toAbsoluteAppPath(relativePath: IPathWrapper): IPathWrapper
    fun appWorkspacesDir(): IPathWrapper
    fun toRelativeAppPath(relativePath: IPathWrapper): IPathWrapper
    fun getFileHash(fullPath: String): String
    fun getFileNodeFromFullPath(workspacePath: String, fullPath: String): FileTreeNode.File
    fun getDirectoryNodeFromFullPath(
        workspacePath: String,
        fullPath: String
    ): FileTreeNode.Directory

    fun getNodes(directoryAbsolutePath: String): List<FileTreeNode>
    fun isNodeExists(path: String): Boolean
    fun deleteNode(nodePath: String)
    fun deleteNodeHeavy(nodePath: String)
    fun moveNode(
        workspacePath: String,
        nodePath: String,
        moveNodePath: String,
        isDirectory: Boolean
    ): ResultWrapper<FileTreeNode, String>

    fun createDirectory(
        fullPath:String
    ): ResultWrapper<Unit, String>

    fun createNode(
        workspacePath: String,
        isDirectory: Boolean,
        nodePath: String,
        byteArray: ByteArray?,
        isMustCreate: Boolean
    ): ResultWrapper<FileTreeNode, String>

    fun setNodeText(nodePath: String, text: String): ResultWrapper<Unit, String>
    fun getNodeText(nodePath: String): String
    fun getNodeBytes(nodePath: String): ByteArray
    fun setNodeBytes(nodePath: String, byteArray: ByteArray)
    fun getNodeCanvas(nodePath: String): ResultWrapper<CanvasDataWithErrors, String>
    fun saveNodeCanvas(nodePath: String, data: CanvasDataWithErrors): ResultWrapper<Unit, String>
}