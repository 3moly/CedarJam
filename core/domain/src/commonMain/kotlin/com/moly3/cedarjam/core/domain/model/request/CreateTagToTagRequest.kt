package com.moly3.cedarjam.core.domain.model.request

data class CreateTagToTagRequest(
    val tagId: Long,
    val tag2Id: Long,
    val createdTime: Long
)