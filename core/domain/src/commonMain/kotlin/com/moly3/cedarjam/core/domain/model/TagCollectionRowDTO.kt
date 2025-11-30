package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TagCollectionRowDTO(
    val id: Long,
    val tagId: Long,
    val rowId: Long
)