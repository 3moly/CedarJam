package com.moly3.cedarjam.core.storage

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.util.IPathWrapper
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors

interface ISystemFilesManager {
    fun toAbsoluteAppPath(relativePath: IPathWrapper): IPathWrapper
    fun toRelativeAppPath(relativePath: IPathWrapper): IPathWrapper
    fun getFileNodeFromFullPath(fullPath: String): FileTreeNode.File
    fun getDirectoryNodeFromFullPath(fullPath: String): FileTreeNode.Directory
    fun getNodes(nodePath: String): List<FileTreeNode>
    fun isNodeExists(path: String): Boolean
    fun deleteNode(nodePath: String)
    fun deleteNodeHeavy(nodePath: String)
    fun moveNode(
        nodePath: String,
        moveNodePath: String,
        isDirectory: Boolean
    ): ResultWrapper<FileTreeNode, String>

    fun createNode(
        isDirectory: Boolean,
        nodePath: String,
        byteArray: ByteArray?
    ): ResultWrapper<FileTreeNode, String>

    fun setNodeText(nodePath: String, text: String): ResultWrapper<Unit, String>
    fun getNodeText(nodePath: String): String
    fun getNodeBytes(nodePath: String): ByteArray
    fun setNodeBytes(nodePath: String, byteArray: ByteArray)
    fun getNodeCanvas(nodePath: String): ResultWrapper<CanvasDataWithErrors, String>
    fun saveNodeCanvas(nodePath: String, data: CanvasDataWithErrors): ResultWrapper<Unit, String>
}