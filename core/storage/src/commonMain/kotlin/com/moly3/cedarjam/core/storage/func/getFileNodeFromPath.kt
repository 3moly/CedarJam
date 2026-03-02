package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.func.relativeTo
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import kotlinx.io.files.Path
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
fun getFileNodeFromPath(
    workspaceFullPath: String,
    filePath: Path,
    isDirectory: Boolean,
    nodes: List<FileTreeNode>? = null,
    fileSize: Long
): FileTreeNode {
    val getOtherFileMeta = getOtherFileMeta(filePath.toString())

    val fullName = filePath.name
    val relativePath = filePath
        .toString()
        .relativeTo(workspaceFullPath)
        .removePrefix("/")
        .removeSuffix(filePath.name)
    val isLastDotExists = fullName.last() == '.'

    val splited = fullName.split(".")
    var shortName = splited.dropLast(1).joinToString(".")
    var fileExtension = splited.takeLast(1).getOrNull(0) ?: ""
    if (shortName.isEmpty() || isDirectory) {
        shortName = fullName
        fileExtension = ""
    }

    val file = if (isDirectory) {
        FileTreeNode.Directory(
            name = fullName,
            parentRelativePath = relativePath,
            workspaceFullPath = workspaceFullPath,
            children = nodes ?: listOf(),
            createdTime = getOtherFileMeta.createdDateTime.toEpochMilliseconds(),
            modifiedTime = getOtherFileMeta.modifiedDateTime.toEpochMilliseconds(),
            fileSize = fileSize
        )
    } else
        FileTreeNode.File(
            workspaceFullPath = workspaceFullPath,
            parentRelativePath = relativePath,
            name = FileName(
                name = shortName + if (isLastDotExists) "." else "",
                extension = fileExtension
            ),
            createdTime = getOtherFileMeta.createdDateTime.toEpochMilliseconds(),
            modifiedTime = getOtherFileMeta.modifiedDateTime.toEpochMilliseconds(),
            fileSize = fileSize
        )
    return file
}

fun getFileNodeFromPath2(
    workspaceFullPath: String,
    filePath: Path,
    currentRelativePath: String,
    parentRelativePath: String,
    isDirectory: Boolean,
    nodes: List<FileTreeNode>,
    fileSize: Long
): FileTreeNode {
    // ⚠️ WARNING: If getOtherFileMeta is a slow disk read, doing this synchronously
    // for thousands of files will freeze your app. Consider lazy loading this.
    val otherFileMeta = getOtherFileMeta(filePath.toString())

    val fullName = filePath.name
    var shortName = fullName
    var fileExtension = ""

    // 3. ZERO-ALLOCATION EXTENSION PARSING
    if (!isDirectory) {
        val lastDotIndex = fullName.lastIndexOf('.')
        // Ensure it's not a hidden file starting with a dot (e.g., ".gitignore") with no extension
        if (lastDotIndex > 0) {
            shortName = fullName.substring(0, lastDotIndex)
            fileExtension = fullName.substring(lastDotIndex + 1)
        }
    }

    val createdTime = otherFileMeta.createdDateTime.toEpochMilliseconds()
    val modifiedTime = otherFileMeta.modifiedDateTime.toEpochMilliseconds()

    return if (isDirectory) {
        FileTreeNode.Directory(
            name = fullName,
            parentRelativePath = parentRelativePath,
            workspaceFullPath = workspaceFullPath,
            children = nodes,
            createdTime = createdTime,
            modifiedTime = modifiedTime,
            fileSize = fileSize
        )
    } else {
        FileTreeNode.File(
            name = FileName(
                name = if (fileExtension.isNotEmpty()) "$shortName." else shortName,
                extension = fileExtension
            ),
            workspaceFullPath = workspaceFullPath,
            parentRelativePath = parentRelativePath,
            createdTime = createdTime,
            modifiedTime = modifiedTime,
            fileSize = fileSize
        )
    }
}