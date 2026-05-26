package com.moly3.cedarjam.core.domain.model.navigation.input

import com.moly3.cedarjam.core.domain.model.CollectionId
import com.moly3.cedarjam.core.domain.model.RowId
import kotlinx.serialization.Serializable

@Serializable
data class CollectionRowPageInput(
    val collectionId: CollectionId,
    val rowId: RowId
)