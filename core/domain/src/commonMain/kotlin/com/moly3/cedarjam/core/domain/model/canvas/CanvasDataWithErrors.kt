package com.moly3.cedarjam.core.domain.model.canvas

import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.dataviz.core.whiteboard.model.ShapeConnection
import com.moly3.dataviz.core.whiteboard.model.StylusPath

data class CanvasDataWithErrors(
    val shapes: List<ResultWrapper<ShapeImpl, CanvasShapeError>>,
    val connections: List<ResultWrapper<ShapeConnection<Long>, CanvasShapeError>>,
    val drawing: List<ResultWrapper<StylusPathImpl, CanvasShapeError>>,
)