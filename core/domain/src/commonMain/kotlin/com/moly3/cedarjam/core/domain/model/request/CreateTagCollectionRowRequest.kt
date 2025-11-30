package com.moly3.cedarjam.core.domain.model.request

data class CreateTagCollectionRowRequest(
    val tagId: Long,
    val rowId: Long,
    val createdTime: Long
)