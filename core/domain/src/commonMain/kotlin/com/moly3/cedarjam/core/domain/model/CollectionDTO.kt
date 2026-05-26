package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class CollectionDTO(
    val id: CollectionId,
    val name: String,
    val viewType: CollectionViewType,
    val createdTime: Long,
    val modifiedTime: Long
)