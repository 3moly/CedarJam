package com.moly3.cedarjam.core.storage

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.util.IPathWrapper

class DemoFilesManagerImpl : ISystemFilesManager {

    private val fs: MutableMap<String, ByteArray?> = mutableMapOf()

    private fun normalize(path: String): String =
        if (path.startsWith("/")) path else "/$path"

    private fun parentOf(path: String): String =
        path.substringBeforeLast("/", "")

    private fun fileNode(path: String, data: ByteArray?): FileTreeNode.File {
        val name = path.substringAfterLast("/")
        val parentPath = parentOf(path)
        val ext = name.substringAfterLast(".", "")
        val baseName = if (ext.isNotEmpty()) name.removeSuffix(".$ext") else name

        return FileTreeNode.File(
            name = FileName(baseName, ext.ifEmpty { null }),
            parentRelativePath = parentPath,
            createdTime = 0L,
            modifiedTime = 0L,
            fileSize = data?.size?.toLong() ?: 0L
        )
    }

    private fun dirNode(path: String, children: List<FileTreeNode>): FileTreeNode.Directory {
        val name = path.substringAfterLast("/", "")
        val parentPath = parentOf(path)

        return FileTreeNode.Directory(
            name = name,
            parentRelativePath = parentPath,
            children = children,
            createdTime = 0L,
            modifiedTime = 0L,
            fileSize = children.sumOf { it.fileSize }
        )
    }


    override fun deleteNode(nodePath: String) {
        fs.remove(normalize(nodePath))
    }

    override fun moveNode(
        workspacePath: String,
        nodePath: String,
        moveNodePath: String,
        isDirectory: Boolean
    ): ResultWrapper<FileTreeNode, String> {
        val from = normalize(nodePath)
        val to = normalize(moveNodePath)
        if (fs.containsKey(to)) throw IllegalArgumentException("Target already exists: $to")

        val data = fs.remove(from)
        fs[to] = data
        return resultBlock {
            if (isDirectory) dirNode(to, listOf()) else fileNode(to, data)
        }
    }

    override fun toAbsoluteAppPath(relativePath: IPathWrapper): IPathWrapper {
        return relativePath
    }

    override fun toRelativeAppPath(relativePath: IPathWrapper): IPathWrapper {
        return relativePath
    }

    override fun getFileNodeFromFullPath(workspacePath: String, fullPath: String): FileTreeNode.File {
        val norm = normalize(fullPath)
        return fileNode(norm, fs[norm] ?: ByteArray(0))
    }

    override fun getDirectoryNodeFromFullPath(workspacePath: String, fullPath: String): FileTreeNode.Directory {
        val norm = normalize(fullPath)
        return dirNode(norm, listOf())
    }

    override fun getNodes(directoryAbsolutePath: String): List<FileTreeNode> {
        val parent = normalize(directoryAbsolutePath).trimEnd('/')
        return fs.keys
            .filter { it.startsWith("$parent/") && it != parent }
            .map {
                val isDir = fs[it] == null
                if (isDir) dirNode(it, listOf())
                else fileNode(it, fs[it])
            }
    }

    override fun isNodeExists(path: String): Boolean =
        fs.containsKey(normalize(path))

    override fun createNode(
        isDirectory: Boolean,
        nodePath: String,
        byteArray: ByteArray?,
        isMustCreate: Boolean
    ): ResultWrapper<FileTreeNode, String> {
        return resultBlock {
            Logger.d("creating node: ${nodePath}")
            val norm = normalize(nodePath)

            ensure(!fs.containsKey(norm)) { "Node $norm already exists" }
            Logger.d("fs: ${fs.size}")
            if (isDirectory) {
                fs[norm] = null
                dirNode(norm, listOf())
            } else {
                fs[norm] = byteArray ?: ByteArray(0)
                fileNode(norm, byteArray)
            }
        }
    }

    override fun setNodeText(nodePath: String, text: String): ResultWrapper<Unit, String> {
        val norm = normalize(nodePath)
        fs[norm] = text.encodeToByteArray()
        return TODO("Provide the return value")
    }

    override fun getNodeText(nodePath: String): String =
        fs[normalize(nodePath)]?.decodeToString()
            ?: throw IllegalArgumentException("Not a file: $nodePath")

    override fun getNodeBytes(nodePath: String): ByteArray =
        fs[normalize(nodePath)] ?: throw IllegalArgumentException("Not a file: $nodePath")

    override fun setNodeBytes(nodePath: String, byteArray: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun getNodeCanvas(nodePath: String): ResultWrapper<CanvasDataWithErrors, String> {
        TODO("Not yet implemented")
    }

    override fun saveNodeCanvas(
        nodePath: String,
        data: CanvasDataWithErrors
    ): ResultWrapper<Unit, String> {
        TODO("Not yet implemented")
    }
}