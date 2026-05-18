package com.moly3.cedarjam.pages.page_workspace.func

import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import kotlinx.collections.immutable.ImmutableList

fun findAndRevealFile(
    targetPath: String,
    files: ImmutableList<FileTreeItemPresentation>,
    openedDirectories: MutableSet<String>
): String? {

    fun searchInTree(
        nodes: ImmutableList<FileTreeItemPresentation>,
        targetPath: String
    ): List<String>? {
        for (node in nodes) {
            if (node.key == targetPath) {
                return listOf(node.key)
            }
            node.children?.let { children ->
                val pathToTarget = searchInTree(children, targetPath)
                if (pathToTarget != null) {
                    // prepend current node to path
                    return listOf(node.key) + pathToTarget
                }
            }
        }
        return null
    }

    val pathInFiles = searchInTree(files, targetPath) ?: return null

    // open all parents, but not the file itself (last element is the target)
    val parentDirectories = pathInFiles.dropLast(1)
    parentDirectories.forEach { dirPath ->
        if (dirPath !in openedDirectories) {
            openedDirectories.add(dirPath)
        }
    }

    return targetPath
}