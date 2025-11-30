package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FileMetadata(
    val path: String,
    val createdTime: Long,
    val modifiedTime: Long,
    val isDeleted: Boolean
)