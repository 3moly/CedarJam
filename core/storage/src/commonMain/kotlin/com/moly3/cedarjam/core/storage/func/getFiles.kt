package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.func.dsStoreFile
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun getFiles(
    parentPath: Path
): Pair<List<FileTreeNode>, Long> {
    var parentFileSize = 0L
    val newFiles = mutableListOf<FileTreeNode>()
    val filesList = SystemFileSystem.list(parentPath)
    for (filePath in filesList) {
        val meta = SystemFileSystem.metadataOrNull(filePath)
        if (meta == null)
            continue

        var nodes: List<FileTreeNode>? = null
        val childPath = Path(parentPath, filePath.name)

        var childFileSize = meta.size

        if (meta.isDirectory) {
            val filesResult = getFiles(childPath)
            childFileSize = filesResult.second
            parentFileSize += filesResult.second
            nodes = filesResult.first
        }
        val node = getFileNodeFromPath(
            filePath,
            meta.isDirectory,
            nodes = nodes,
            fileSize = childFileSize
        )
        when (node) {
            is FileTreeNode.Directory -> {}
            is FileTreeNode.File -> {
                if (node.getFullName() == dsStoreFile)
                    continue
            }
        }
        if (!meta.isDirectory) {
            parentFileSize += meta.size
        }
        newFiles.add(node)
    }
    return Pair(newFiles, parentFileSize)
}