package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionDTO(
    val id: Long,
    val name: String,
    val viewType: CollectionViewType,
    val createdTime: Long,
    val modifiedTime: Long
)