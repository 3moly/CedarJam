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
    val createdTime: Long,
    val modifiedTime: Long,
    val isDirectory: Boolean,
    val size: Long,
    val isDeleted: Boolean
)