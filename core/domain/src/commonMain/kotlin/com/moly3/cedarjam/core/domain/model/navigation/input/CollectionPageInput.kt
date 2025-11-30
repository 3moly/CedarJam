package com.moly3.cedarjam.core.domain.model.navigation.input

import kotlinx.serialization.Serializable

@Serializable
data class CollectionPageInput(
    val collectionId: Long
)