package com.moly3.cedarjam.core.domain.model

data class TagAnnotationDTO(
    val id: TagAnnotationId,
    val tagId: TagId,
    val annotationId: AnnotationId
)