package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.indexdb.IndexDatabase

// Enum for readability (maps to your INTEGER columns)


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

fun updateIndex(
    localNodes: List<FileTreeNode>,
    serverNodes: List<FileItem>,
    dbHelper: IndexDatabase // Wrapper around your SQLDelight Database
) {
    // 1. Prepare Data Structures
    val onDiskFiles = localNodes.flattenToMap()
    val serverFiles = serverNodes.associateBy { it.relativePath }
    
    // 2. Run inside a Transaction for performance and consistency
    dbHelper.indexFileQueries.transaction {
        val dbQueries =dbHelper.indexFileQueries
        
        // Fetch current DB state
        val dbRecords = dbQueries.selectAll().executeAsList().associateBy { it.relativePath }
        
        // --- PHASE A: Handle Files Present on Disk ---
        onDiskFiles.forEach { (path, node) ->
            val dbRecord = dbRecords[path]
            val serverRecord = serverFiles[path]
            
            val isDirectory = if (node.isDirectory()) 1L else 0L
            
            // Logic to calculate Hash:
            // Only calculate if file is NEW or ModifiedTime/Size changed.
            // In a real app, do the hashing in a background thread/Job, not here in the DB transaction.
            // For this example, we assume we have a helper `calculateHashOrNull`.
            
            if (dbRecord == null) {
                // CASE 1: NEW FILE (On Disk, Not in DB)
                // If it exists on server with same hash -> It's SYNCED (we just missed it locally)
                // Otherwise -> It's NEW
                
                val currentHash = calculateHash(node) 
                
                // Check if we are actually just downloading a file that exists on server
                val status = if (serverRecord != null && serverRecord.contentHash == currentHash) {
                     SyncStatus.SYNCED
                } else {
                     SyncStatus.NEW
                }


                dbQueries.insertItem(
                    relativePath = path,
                    contentHash = currentHash,
                    modifiedTime = node.modifiedTime,
                    size = node.fileSize,
                    isDirectory = isDirectory,
                    lastSyncedHash = serverRecord?.contentHash, // If on server, this is our base
                    serverSyncStatus = status.code
                )
            } else {
                // CASE 2: EXISTING FILE (On Disk, In DB)
                
                // Check if modified locally
                val isModified = node.modifiedTime != dbRecord.modifiedTime || node.fileSize != dbRecord.size
                
                if (isModified) {
                    // It changed on disk!
                    val newHash = calculateHash(node)
                    
                    // Check for CONFLICT:
                    // If Server has changed since we last synced (serverHash != lastSyncedHash)
                    // AND we have also changed (isModified) -> Conflict logic is usually handled in UI or separate logic.
                    // Here we just mark as DIRTY (Modified).
                    
                    val status = if (newHash == dbRecord.lastSyncedHash) SyncStatus.SYNCED else SyncStatus.DIRTY
                    
                    dbQueries.updateItem(
                        contentHash = newHash,
                        modifiedTime = node.modifiedTime,
                        size = node.fileSize,
                        serverSyncStatus = status.code,
                        relativePath = path
                    )
                } else {
                    // File on disk matches DB. 
                    // But wait... did we previously mark it as DELETED? If so, user restored it.
                    if (dbRecord.serverSyncStatus == SyncStatus.DELETED.code) {
                         dbQueries.updateStatus(SyncStatus.DIRTY.code, path)
                    }
                }
            }
        }

        // --- PHASE B: Handle Files Missing from Disk (Deletions) ---
        dbRecords.keys.forEach { path ->
            if (!onDiskFiles.containsKey(path)) {
                val dbRecord = dbRecords[path]!!
                
                // If it's already marked DELETED, ignore.
                // If it was NEW (never synced), just remove from DB entirely.
                // If it was SYNCED or DIRTY, mark as DELETED.
                
                if (dbRecord.serverSyncStatus == SyncStatus.NEW.code) {
                    dbQueries.deleteItem(path)
                } else if (dbRecord.serverSyncStatus != SyncStatus.DELETED.code) {
                    dbQueries.updateStatus(SyncStatus.DELETED.code, path)
                }
            }
        }
        
        // --- PHASE C: Handle Server-Only Files (Downloads) ---
        // These are files on Server, but NOT in DB and NOT on Disk.
        // Usually, we don't insert them into "IndexFile" until we actually download them.
        // But if you want to show them in UI as "Cloud Only", you might handle them here or in a separate query.
    }
}

// Pseudo-code helper for Hashing
fun calculateHash(node: FileTreeNode): String? {
    if (node.isDirectory()) return null
    // Use your Okio/KMP hashing logic here
    // return FileSystem.SYSTEM.read(node.getFullPath().toPath()) { ... }
    return calculateFileHash(node.getFullPath())
}