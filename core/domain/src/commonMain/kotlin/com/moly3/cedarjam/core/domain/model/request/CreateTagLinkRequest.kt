package com.moly3.cedarjam.core.domain.model.request

data class CreateTagLinkRequest(
    val fullPath: String,
    val tagId: Long
)