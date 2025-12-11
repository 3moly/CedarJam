package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.indexdb.IndexDatabase

fun updateIndex(
    localNodes: List<FileTreeNode>,
    serverNodes: List<FileItem>,
    dbHelper: IndexDatabase
) {
    // 1. Prepare Data Structures
    val onDiskFiles = localNodes.flattenToMap()
    val serverFiles = serverNodes.associateBy { it.relativePath }

    dbHelper.indexFileQueries.transaction {
        val dbQueries = dbHelper.indexFileQueries

        // FIX: Remove the filter. We need ALL records to detect changes to SYNCED files.
        val dbRecords = dbQueries.selectAll().executeAsList().associateBy { it.relativePath }

        // --- PHASE A: Handle Files Present on Disk ---
        onDiskFiles.forEach { (path, node) ->
            val dbRecord = dbRecords[path]
            val serverRecord = serverFiles[path]

            val isDirectory = if (node.isDirectory()) 1L else 0L

            if (dbRecord == null) {
                // CASE 1: TRULY NEW FILE (Not in DB at all)
                val currentHash = calculateHash(node)

                // If the file exists on server with exact same hash, we are in sync.
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
                    lastSyncedHash = serverRecord?.contentHash,
                    serverSyncStatus = status.code
                )
            } else {
                // CASE 2: EXISTING FILE (Could be SYNCED, NEW, DIRTY, or DELETED)

                // 2.1 Handle "Ghost" Deletions
                // If it was marked DELETED in DB, but User recreated it (or we missed the delete)
                // We treat it as a modification/restoration.
                if (dbRecord.serverSyncStatus == SyncStatus.DELETED.code) {
                    val currentHash = calculateHash(node)
                    // If user restored exactly what was there before delete, it might be SYNCED?
                    // Usually safer to mark DIRTY or NEW. Let's mark DIRTY.
                    dbQueries.updateItem(
                        contentHash = currentHash,
                        modifiedTime = node.modifiedTime,
                        size = node.fileSize,
                        serverSyncStatus = SyncStatus.DIRTY.code,
                        relativePath = path
                    )
                }
                // 2.2 Standard Check
                else {
                    // Check modification against the DB record
                    val isModified = node.modifiedTime != dbRecord.modifiedTime ||
                            node.fileSize != dbRecord.size

                    if (isModified) {
                        // File changed on disk vs what we remember
                        val newHash = calculateHash(node)

                        // Smart Check:
                        // Maybe we reverted the file to the state of last sync?
                        // If current hash == lastSyncedHash, then we are actually SYNCED again.
                        val status = if (newHash == dbRecord.lastSyncedHash) {
                            SyncStatus.SYNCED
                        } else {
                            SyncStatus.DIRTY
                        }

                        dbQueries.updateItem(
                            contentHash = newHash,
                            modifiedTime = node.modifiedTime,
                            size = node.fileSize,
                            serverSyncStatus = status.code,
                            relativePath = path
                        )
                    } else {
                        // File is strictly identical to DB record.
                        // Do nothing. Ideally, verify if dbRecord.serverSyncStatus is correct.
                    }
                }
            }
        }

        // --- PHASE B: Handle Files Missing from Disk (Deletions) ---
        // Iterate over ALL DB records. If it's in DB but not on Disk -> Deleted.
        dbRecords.keys.forEach { path ->
            if (!onDiskFiles.containsKey(path)) {
                val dbRecord = dbRecords[path]!!

                if (dbRecord.serverSyncStatus == SyncStatus.NEW.code) {
                    // It was never sent to server, so we can just forget it.
                    dbQueries.deleteItem(path)
                } else if (dbRecord.serverSyncStatus != SyncStatus.DELETED.code) {
                    // It was SYNCED or DIRTY. Now it is DELETED.
                    // This line executes now because we included SYNCED files in dbRecords!
                    dbQueries.updateStatus(SyncStatus.DELETED.code, path)
                }
            }
        }
    }
}