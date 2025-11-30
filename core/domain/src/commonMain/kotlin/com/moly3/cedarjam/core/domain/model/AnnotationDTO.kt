package com.moly3.cedarjam.core.domain.model

data class AnnotationDTO(
    val id: Long,
    val dataPath: String,
    val dataPoint: Double,
    val description: String
)