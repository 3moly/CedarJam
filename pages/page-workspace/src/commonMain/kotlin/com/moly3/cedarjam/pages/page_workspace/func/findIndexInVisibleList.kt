package com.moly3.cedarjam.pages.page_workspace.func

import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import kotlinx.collections.immutable.ImmutableList

fun findIndexInVisibleList(
    key: String,
    files: ImmutableList<FileTreeItemPresentation>,
    openedDirectories: ImmutableList<String>
): Int? {
    var index = 0

    fun traverse(nodes: ImmutableList<FileTreeItemPresentation>): Int? {
        for (node in nodes) {
            if (node.key == key) {
                return index
            }
            index++
            val nodeChildren = node.children
            if (nodeChildren != null &&
                node.key in openedDirectories
            ) {
                val found = traverse(nodeChildren)
                if (found != null) return found
            }
        }
        return null
    }

    return traverse(files)
}