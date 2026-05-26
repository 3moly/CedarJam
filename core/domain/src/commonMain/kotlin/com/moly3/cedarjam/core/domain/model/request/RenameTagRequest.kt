package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.TagId

data class RenameTagRequest(
    val id: TagId,
    val newName: String,
    val modifiedTime: Long
)