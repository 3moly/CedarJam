package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.model.FileTreeNode

fun List<FileTreeNode>.insertNode(
    newNode: FileTreeNode,
    targetParentRelativePath: String
): List<FileTreeNode> {
    return map { node ->
        when (node) {
            is FileTreeNode.Directory if node.getRelativePath() == targetParentRelativePath -> {
                node.copy(
                    children = node.children + newNode,
                    fileSize = node.fileSize + newNode.fileSize
                )
            }
            is FileTreeNode.Directory -> {
                val updatedChildren = node.children.insertNode(newNode, targetParentRelativePath)
                node.copy(
                    children = updatedChildren,
                    fileSize = updatedChildren.sumOf { it.fileSize }
                )
            }
            else -> node
        }
    }
}