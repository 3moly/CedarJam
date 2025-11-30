package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TagToTagDTO(
    val id: Long,
    val firstTagId: Long,
    val secondTagId: Long
)