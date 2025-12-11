package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.FileTreeNode

// Pseudo-code helper for Hashing
fun calculateHash(node: FileTreeNode): String? {
    if (node.isDirectory()) return null
    // Use your Okio/KMP hashing logic here
    // return FileSystem.SYSTEM.read(node.getFullPath().toPath()) { ... }
    return calculateFileHash(node.getFullPath())
}