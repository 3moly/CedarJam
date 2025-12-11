package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.FileTreeNode


// Helper to flatten the tree into a Map<RelativePath, Node>
fun List<FileTreeNode>.flattenToMap(): Map<String, FileTreeNode> {
    val result = mutableMapOf<String, FileTreeNode>()

    fun traverse(nodes: List<FileTreeNode>) {
        for (node in nodes) {
            // Normalize path (remove leading slashes, handle windows separators if needed)
            val path = node.getRelativePath().replace("\\", "/")
            result[path] = node

            if (node is FileTreeNode.Directory) {
                traverse(node.children)
            }
        }
    }
    traverse(this)
    return result
}