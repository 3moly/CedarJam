package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.CollectionViewType

data class UpdateDataCollectionRequest(
    val id: Long,
    val viewType: CollectionViewType,
    val modifiedTime: Long
)