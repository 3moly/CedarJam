package com.moly3.cedarjam.core.domain.model.canvas

import com.moly3.dataviz.core.whiteboard.model.StylusPath
import com.moly3.dataviz.core.whiteboard.model.StylusPoint
import kotlinx.serialization.Serializable

@Serializable
data class StylusPointImpl(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val tiltX: Float = 0f,
    val tiltY: Float = 0f,
    val strokeWidth: Float = 5f,
    val timestamp: Long
)

fun StylusPoint.toImpl(): StylusPointImpl {
    return StylusPointImpl(
        x = x,
        y = y,
        pressure = pressure,
        tiltX = tiltX,
        tiltY = tiltY,
        strokeWidth = strokeWidth,
        timestamp = timestamp
    )
}

fun StylusPointImpl.toSimple(): StylusPoint {
    return StylusPoint(
        x = x,
        y = y,
        pressure = pressure,
        tiltX = tiltX,
        tiltY = tiltY,
        strokeWidth = strokeWidth,
        timestamp = timestamp
    )
}