package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.AnnotationId
import com.moly3.cedarjam.core.domain.model.TagId

data class CreateTagAnnotationRequest(
    val tagId: TagId,
    val annotationId: AnnotationId,
    val createdTime: Long
)