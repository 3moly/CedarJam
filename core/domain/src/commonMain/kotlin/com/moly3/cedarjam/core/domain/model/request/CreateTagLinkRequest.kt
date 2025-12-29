package com.moly3.cedarjam.core.domain.model.request

data class CreateTagLinkRequest(
    val relativePath: String,
    val tagId: Long
)