package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.TagId

data class CreateTagLinkRequest(
    val relativePath: String,
    val tagId: TagId
)