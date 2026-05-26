package com.moly3.cedarjam.core.domain.model.navigation.input

import com.moly3.cedarjam.core.domain.model.TagId
import kotlinx.serialization.Serializable

@Serializable
data class TagPageInput(
    val id: TagId,
    val isOpenGraphDialog: Boolean = false
)