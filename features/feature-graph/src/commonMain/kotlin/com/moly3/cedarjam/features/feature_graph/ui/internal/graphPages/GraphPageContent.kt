package com.moly3.cedarjam.features.feature_graph.ui.internal.graphPages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.ui.uikit.CJIOSwitch
import com.moly3.cedarjam.core.ui.uikit.CJSlider
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.features.feature_graph.Intent
import com.moly3.cedarjam.features.feature_graph.State
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import com.moly3.dataviz.graph.ui.Graph

@Composable
internal fun GraphPageContent(
    state: State,
    engine: IGraphEngine<String, ObsidianGraphData>,
    onIntent: (Intent) -> Unit
) {
    val centerGlobalPosition = remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        Graph(
            engine = engine,
            connections = state.connections,
            stateNodes = state.graphNodes,
            coordinates = state.coordinates,
            zoom = state.zoom,
            onZoomChange = { isWhat, position ->
                onIntent(Intent.SetZoom(position.coerceIn(0.5f, 3f)))
            },
            watchNodeId = state.graphTargetId,
            userPosition = centerGlobalPosition.value,
            onWatchPosition = { nodePosition ->
                centerGlobalPosition.value = nodePosition
            },
            onPanDelta = { delta ->
                centerGlobalPosition.value += delta
            },
            onCoordinatesUpdate = {
                onIntent(Intent.SetCoordinates(it))
            },
            velocities = state.velocities,
            onNodeClick = { node ->
                onIntent(Intent.OpenNode(node.data))
            },
            io = io,
//            fontColor = LocalAppTheme.current.colors.primaryFont,
//            circleColor = Color.Yellow,
//            primaryColor = Color.Blue,
//            circleLineColor = LocalAppTheme.current.colors.divide,
//            textStyle = LocalTextStyle.current
            consume = false

        )
        Column(
            Modifier.align(Alignment.TopEnd).width(300.dp),
            horizontalAlignment = Alignment.End
        ) {
            CJText(text = state.zoom.toString())
            CJSlider(
                modifier = Modifier.width(100.dp).padding(4.dp),
                value = state.zoom,
                onValueChange = {
                    onIntent(Intent.SetZoom(it))
                },
                valueRange = 0.5f..3f
            )
            CJIOSwitch(
                height = 36,
                checked = state.isShowNestedConnections
            ) {
                onIntent(Intent.SetIsShowNestedConnections(!state.isShowNestedConnections))
            }
        }
    }
}