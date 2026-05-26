package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.RowId

data class RenameDataCollectionRowRequest(
    val rowId: RowId,
    val newName: String,
    val modifiedTime: Long
)