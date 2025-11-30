package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeletedFileMetadata(
    val relativePath: String,
    val deletedTime: Long
)