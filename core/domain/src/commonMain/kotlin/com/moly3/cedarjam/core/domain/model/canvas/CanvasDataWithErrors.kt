package com.moly3.cedarjam.core.domain.model.canvas

import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.dataviz.core.block.model.ShapeConnection

data class CanvasDataWithErrors(
    val shapes: List<ResultWrapper<ShapeImpl, CanvasShapeError>>,
    val connections: List<ResultWrapper<ShapeConnection, CanvasShapeError>>
)