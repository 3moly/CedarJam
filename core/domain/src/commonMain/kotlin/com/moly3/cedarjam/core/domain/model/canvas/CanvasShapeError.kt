package com.moly3.cedarjam.core.domain.model.canvas

import kotlinx.serialization.json.JsonElement

data class CanvasShapeError(
    val rawJson: JsonElement,
    val error: String,
    val position: Position,
    val size: Size,
    val id: Long
)