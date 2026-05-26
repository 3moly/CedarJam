package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TagToTagDTO(
    val id: TagToTagId,
    val firstTagId: TagId,
    val secondTagId: TagId
)