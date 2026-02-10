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
    val fullName = filePath.name
    val relativePath = filePath
        .toString()
        .relativeTo(workspaceFullPath)
        .removePrefix("/")
        .removeSuffix(filePath.name)
//    val parentFullPath = filePath.parent?.toString() ?: ""
    val isLastDotExists = fullName.last() == '.'

    val splited = fullName.split(".")
    var shortName = splited.dropLast(1).joinToString(".")
    var fileExtension = splited.takeLast(1).getOrNull(0) ?: ""
    if (shortName.isEmpty() || isDirectory) {
        shortName = fullName
        fileExtension = ""
    }
    val getOtherFileMeta = getOtherFileMeta(filePath.toString())

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