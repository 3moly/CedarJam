package com.moly3.cedarjam.core.domain.model

import kotlin.time.ExperimentalTime
import kotlinx.serialization.Serializable

@Serializable
data class FileStructure @OptIn(ExperimentalTime::class) constructor(
    val modifiedTime: Long,
    val files: List<FileItem>
)

@Serializable
data class FileItem(
    val relativePath: String,
    val contentHash: String? = null,
    val modifiedTime: Long,
    val isDeleted: Boolean,
    val isDirectory: Boolean,
    val size: Long? = null
)