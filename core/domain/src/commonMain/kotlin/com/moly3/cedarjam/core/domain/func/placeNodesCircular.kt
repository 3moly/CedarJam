package com.moly3.cedarjam.core.domain.func

import androidx.compose.ui.geometry.Offset
import com.moly3.dataviz.core.graph.model.GraphNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun <Id, Data> placeNodesCircular(
    stateNodes: List<GraphNode<Id, Data>>,
    changeCoords: MutableMap<Id, Offset>,
    centerX: Float = 0f,
    centerY: Float = 0f,
    startRadius: Float = 300f,
    spacing: Float = 5f // distance between spiral turns
) {
    var angle = 0f
    var radius = startRadius

    val goldenAngle = (PI * (3.0 - sqrt(5.0))).toFloat() // good distribution

    for (node in stateNodes) {
        if (changeCoords[node.id] == null) {
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            changeCoords[node.id] = Offset(x, y)

            // Spiral out
            radius += spacing / (2 * PI).toFloat()
            angle += goldenAngle
        }
    }
}