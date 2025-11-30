package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.Workspace

fun FileTreeNode.getRelativePath(
    filePath: FileTreeNode = this,
    workspace: Workspace
): String {
    return filePath
        .getFullPath()
        .replaceFirst(workspace.fullpath, "")
        .replaceFirst("/", "")
}

fun getRelativePath(filePath: FileTreeNode, workspace: Workspace): String {
    return filePath
        .getFullPath()
        .replaceFirst(workspace.fullpath, "")
        .replaceFirst("/", "")
}

fun FileTreeNode.getRelativePath(filePath: FileTreeNode = this, workspacePath: String): String {
    return filePath
        .getFullPath()
        .replaceFirst(workspacePath, "")
        .replaceFirst("/", "")
}

fun getRelativePath(fullPath: String, workspacePath: String): String {
    return fullPath
        .replaceFirst(workspacePath, "")
        .replaceFirst("/", "")
}