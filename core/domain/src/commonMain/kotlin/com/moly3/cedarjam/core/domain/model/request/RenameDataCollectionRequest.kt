package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.CollectionId

data class RenameDataCollectionRequest(
    val id: CollectionId,
    val newName: String,
    val modifiedTime: Long
)