package com.moly3.cedarjam.core.storage.internal

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.filesDirPath
import com.moly3.cedarjam.core.storage.func.getFileNodeFromPath
import com.moly3.cedarjam.core.storage.func.getFiles
import com.moly3.cedarjam.core.domain.util.IPathWrapper
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.util.PathWrapper
import com.moly3.cedarjam.core.storage.json.canvas.CanvasDataParser
import com.moly3.cedarjam.core.domain.model.canvas.CanvasDataWithErrors
import com.moly3.cedarjam.core.storage.func.calculateFileHash
import com.moly3.cedarjam.core.storage.func.setLastWriteTimeUtc
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.utils.toPath
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.io.writeString

internal class SystemFilesManager : ISystemFilesManager {

    private val fs: FileSystem = SystemFileSystem
    private fun copyFile(sourcePath: ByteArray, destinationPath: String) {

        val destination = Path(destinationPath)
        val parentDir = destination.parent
        if (parentDir != null && !fs.exists(parentDir)) {
            fs.createDirectories(parentDir)
        }
        if(fs.exists(destination)){
            fs.delete(destination)
        }
        fs.sink(destination).buffered().use { destinationBuffer ->
            destinationBuffer.write(sourcePath)
        }
    }

    private fun writeText(filePath: Path, content: String): ResultWrapper<Unit, String> {
        return resultBlock {
            try {
                val sink = fs.sink(filePath)
                val bufferedSink = sink.buffered()
                bufferedSink.writeString(content)
                bufferedSink.flush()
                bufferedSink.close()
            } catch (exc: FileNotFoundException) {
                raise(exc.toString())
            }
        }
    }

    override fun toAbsoluteAppPath(relativePath: IPathWrapper): IPathWrapper {
        return when (getPlatform()) {
            Platform.Android,
            Platform.Ios -> {
                val firstPart = FileKit.filesDirPath()
                if (relativePath.toString().contains(firstPart))
                    pathWrapper(relativePath.toString())
                else
                    pathWrapper(
                        firstPart,
                        relativePath.toString()
                    )
            }

            is Platform.Jvm,
            Platform.Wasm -> {
                pathWrapper(relativePath.pathString)
            }
        }
    }

    override fun toRelativeAppPath(relativePath: IPathWrapper): IPathWrapper {
        return when (getPlatform()) {
            Platform.Android,
            Platform.Ios -> {
                val relativePath = if (relativePath.pathString.first() == '/') {
                    relativePath.pathString.replaceFirst("/", "")
                } else relativePath.pathString
                pathWrapper(relativePath)
            }

            is Platform.Jvm,
            Platform.Wasm -> pathWrapper(relativePath.pathString)
        }
    }

    override fun getFileHash(fullPath: String): String {
        return calculateFileHash(fullPath)
    }

    override fun getFileNodeFromFullPath(workspacePath: String, fullPath: String): FileTreeNode.File {
        return getFileNodeFromPath(
            workspaceFullPath = workspacePath,
            Path(fullPath),
            false,
            fileSize = 0L
        ) as FileTreeNode.File
    }

    override fun getDirectoryNodeFromFullPath(
        workspacePath: String,
        fullPath: String
    ): FileTreeNode.Directory {
        val abs = toAbsoluteAppPath(pathWrapper(fullPath))
        if (!isNodeExists(abs.pathString)) {
            createNode(workspacePath, true, abs.pathString, byteArray = null, isMustCreate = true)
        }
        return getFileNodeFromPath(
            workspaceFullPath = workspacePath,
            abs.pathString.toPath(),
            true,
            fileSize = 0L
        ) as FileTreeNode.Directory
    }

    override fun deleteNode(nodePath: String) {
        try {
            fs.delete(Path(nodePath), true)
        }catch (exc: Exception){}
    }

    override fun deleteNodeHeavy(nodePath: String) {

        try {
            val path = Path(nodePath)
            if (fs.metadataOrNull(path)!!.isDirectory) {
                val child = fs.list(Path(nodePath))
                for (childPath in child) {
                    deleteNodeHeavy(childPath.toString())
                }
            }
            fs.delete(Path(nodePath), true)
        } catch (exc: Exception) {
        }
    }

    override fun moveNode(
        workspacePath: String,
        nodePath: String,
        moveNodePath: String,
        isDirectory: Boolean
    ): ResultWrapper<FileTreeNode, String> {
        return resultBlock {
            val path = Path(nodePath)
            val newFilePath = Path(moveNodePath)
            ensure(!fs.exists(newFilePath)) {
                "to move node is already exists $moveNodePath"
            }

            val isTrans = moveNodePath.contains(nodePath)
            ensure(!isTrans || path.parent.toString() == newFilePath.parent.toString()) { "cannot move to child directory" }

            fs.atomicMove(path, newFilePath)
            setLastWriteTimeUtc(newFilePath.toString(), nowInMs())
            getFileNodeFromPath(
                workspaceFullPath = workspacePath,
                filePath = newFilePath,
                isDirectory = isDirectory,
                fileSize = 0L
            )
        }
    }

    override fun getNodes(directoryAbsolutePath: String): List<FileTreeNode> {
        return getFiles(
            workspaceFullPath = directoryAbsolutePath,
            parentPath = Path(directoryAbsolutePath)
        ).first
    }

    override fun isNodeExists(path: String): Boolean {
        Logger.w {
            "isNodeExists: ${path}"
        }
        val path = path.toPath()
        val absolutePath = if (path.isAbsolute) {
            path.toString()
        } else {
            toAbsoluteAppPath(pathWrapper(path.toString())).pathString
        }
        return fs.exists(absolutePath.toPath())
    }

    override fun createDirectory(fullPath: String): ResultWrapper<Unit, String> {
        return resultBlock {
            fs.createDirectories(Path(fullPath),mustCreate = true)
        }
    }

    override fun createNode(
        workspacePath: String,
        isDirectory: Boolean,
        nodePath: String,
        byteArray: ByteArray?,
        isMustCreate: Boolean
    ): ResultWrapper<FileTreeNode, String> {
        Logger.w {
            "create node: ${nodePath}"
        }
        val nodePath = toAbsoluteAppPath(PathWrapper(nodePath.toPath())).pathString
        return resultBlock {
            val path = Path(nodePath)
            val meta = fs.metadataOrNull(path)
            if (meta != null && isMustCreate) {
                if (isDirectory != meta.isDirectory) {
                    fs.delete(path)
                }
            }
            ensure(!(fs.exists(path) && meta?.isDirectory == isDirectory)) {
                "node $path is already exists"
            }
            if (isDirectory) {
                fs.createDirectories(path)
            } else {
                val parent = path.parent
                if (parent != null && !fs.exists(parent)) {
                    fs.createDirectories(parent)
                }

                val writeTextResult = writeText(path, "")
                bind(writeTextResult)
            }
            val se = getFileNodeFromPath(
                workspaceFullPath = workspacePath,
                filePath = path,
                isDirectory = isDirectory,
                fileSize = 0L
            )
            if (byteArray != null) {
                copyFile(byteArray, se.getFullPath())
            }
            se
        }
    }

    override fun setNodeBytes(nodePath: String, byteArray: ByteArray) {
        copyFile(byteArray, nodePath)
    }

    override fun setNodeText(nodePath: String, text: String): ResultWrapper<Unit, String> {
        return writeText(Path(nodePath), text)
    }

    override fun getNodeText(nodePath: String): String {
        return fs.source(Path(nodePath)).use { source ->
            val bufferedSource = source.buffered()
            bufferedSource.readString()
        }
    }

    override fun getNodeBytes(nodePath: String): ByteArray {
        return fs.source(Path(nodePath)).use { source ->
            val bufferedSource = source.buffered()
            bufferedSource.readByteArray()
        }
    }

    override fun getNodeCanvas(nodePath: String): ResultWrapper<CanvasDataWithErrors, String> {
        return resultBlock {
            val json = getNodeText(nodePath = nodePath)
            CanvasDataParser.parse(json)
        }
    }

    override fun saveNodeCanvas(
        nodePath: String,
        data: CanvasDataWithErrors
    ): ResultWrapper<Unit, String> {
        return resultBlock {
            val json = CanvasDataParser.serialize(data)
            setNodeText(nodePath = nodePath, text = json)
        }
    }
}