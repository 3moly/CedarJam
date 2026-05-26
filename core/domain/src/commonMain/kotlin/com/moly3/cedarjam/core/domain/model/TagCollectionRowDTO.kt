package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TagCollectionRowDTO(
    val id: TagRowId,
    val tagId: TagId,
    val rowId: RowId
)