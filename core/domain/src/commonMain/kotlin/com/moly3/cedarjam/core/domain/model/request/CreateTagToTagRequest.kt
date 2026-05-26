package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.TagId

data class CreateTagToTagRequest(
    val tagId: TagId,
    val tag2Id: TagId,
    val createdTime: Long
)