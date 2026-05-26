package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.model.RowId

data class CreateAnnotationRequest(
    val dataPath: String,
    val description: String,
    val dataPoint: Double,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rowId: RowId?
)