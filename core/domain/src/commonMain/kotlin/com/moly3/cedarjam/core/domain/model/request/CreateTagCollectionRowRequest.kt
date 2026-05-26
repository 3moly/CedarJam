package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.RowId
import com.moly3.cedarjam.core.domain.model.TagId

data class CreateTagCollectionRowRequest(
    val tagId: TagId,
    val rowId: RowId,
    val createdTime: Long
)