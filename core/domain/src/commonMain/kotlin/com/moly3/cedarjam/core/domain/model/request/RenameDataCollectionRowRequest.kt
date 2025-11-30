package com.moly3.cedarjam.core.domain.model.request

data class RenameDataCollectionRowRequest(
    val rowId: Long,
    val newName: String,
    val modifiedTime: Long
)