package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FileMetadata(
    val relativePath: String,
    val modifiedTime: Long,
    val contentHash: String,
    val isDeleted: Boolean,
    val isDirectory: Boolean
)