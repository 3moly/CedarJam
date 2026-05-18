package com.moly3.cedarjam.core.domain.model

data class IndexFileDto(
    val relativePath: String,
    val contentHash: String?,
    val modifiedTime: Long,
    val size: Long?,
    val isDirectory: Boolean,
    val lastSyncedHash: String?,
    val serverSyncStatus: SyncStatus?
)