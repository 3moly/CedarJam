package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable

@Stable
data class AnnotationDTO(
    val id: Long,
    val dataPath: String,
    val dataPoint: Double,
    val description: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val modifiedTime: Long,
    val rowId: Long?
)