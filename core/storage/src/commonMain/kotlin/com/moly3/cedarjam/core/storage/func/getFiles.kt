package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.func.ignoreByRelativePath
import com.moly3.cedarjam.core.domain.func.ignoredDsStoreFile
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun getFiles(
    workspaceFullPath: String,
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
            val filesResult = getFiles(workspaceFullPath = workspaceFullPath, childPath)
            childFileSize = filesResult.second
            parentFileSize += filesResult.second
            nodes = filesResult.first
        }
        val node = getFileNodeFromPath(
            workspaceFullPath = workspaceFullPath,
            filePath = filePath,
            meta.isDirectory,
            nodes = nodes,
            fileSize = childFileSize
        )
        when (node) {
            is FileTreeNode.Directory -> {}
            is FileTreeNode.File -> {
                val relativePath = node.getRelativePath()
                if (ignoreByRelativePath.firstOrNull { d -> d == relativePath } != null)
                    continue
                if (node.getFullName() == ignoredDsStoreFile)
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

class DirectoryParseResult(
    val nodes: List<FileTreeNode>,
    val totalSize: Long
)

fun getFiles2(
    workspaceFullPath: String,
    parentPath: Path,
    parentRelativePath: String = "" // Pass this down to avoid string recalculations
): DirectoryParseResult {
    var parentFileSize = 0L
    val newFiles = mutableListOf<FileTreeNode>()
    val filesList = SystemFileSystem.list(parentPath)

    for (filePath in filesList) {
        val fileName = filePath.name

        // 1. EARLY FILTERING: Ignore exact matches immediately
        if (fileName == ignoredDsStoreFile) continue

        // Calculate relative path cheaply by appending
        val currentRelativePath = if (parentRelativePath.isEmpty()) {
            fileName
        } else {
            "$parentRelativePath/$fileName"
        }

        // 2. EARLY FILTERING: Fast O(1) Set lookup
        // if (ignoreByRelativePathSet.contains(currentRelativePath)) continue

        val meta = SystemFileSystem.metadataOrNull(filePath) ?: continue

        var childNodes: List<FileTreeNode> = emptyList()
        var nodeSize = meta.size

        if (meta.isDirectory) {
            val childResult = getFiles2(workspaceFullPath, filePath, currentRelativePath)
            childNodes = childResult.nodes
            nodeSize = childResult.totalSize
            parentFileSize += nodeSize
        } else {
            parentFileSize += nodeSize
        }

        val node = getFileNodeFromPath2(
            workspaceFullPath = workspaceFullPath,
            filePath = filePath,
            currentRelativePath = currentRelativePath, // Pass the pre-calculated string
            parentRelativePath = parentRelativePath,   // Pass the pre-calculated string
            isDirectory = meta.isDirectory,
            nodes = childNodes,
            fileSize = nodeSize
        )

        newFiles.add(node)
    }

    return DirectoryParseResult(newFiles, parentFileSize)
}