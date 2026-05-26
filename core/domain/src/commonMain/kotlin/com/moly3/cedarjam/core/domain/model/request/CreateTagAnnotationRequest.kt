package com.moly3.cedarjam.core.domain.model.request

data class CreateTagAnnotationRequest(
    val tagId: Long,
    val annotationId: Long,
    val createdTime: Long
)