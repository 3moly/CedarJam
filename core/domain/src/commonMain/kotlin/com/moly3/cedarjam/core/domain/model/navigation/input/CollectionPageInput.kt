package com.moly3.cedarjam.core.domain.model.navigation.input

import com.moly3.cedarjam.core.domain.model.CollectionId
import kotlinx.serialization.Serializable

@Serializable
data class CollectionPageInput(
    val collectionId: CollectionId
)