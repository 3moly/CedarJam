package com.moly3.cedarjam.pages.page_graph

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.node.GraphSettingsConfig
import com.moly3.cedarjam.core.domain.model.OffsetData
import com.moly3.cedarjam.core.domain.model.mapToOffset
import com.moly3.cedarjam.core.domain.model.mapToOffsetData
import com.moly3.dataviz.core.graph.model.GraphViewSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable

data class State(
    val isShowSettings: Boolean = false,
    val config: GraphSettingsConfig = GraphSettingsConfig.Default,
    val graphNodes: ImmutableList<ObsidianGraphNode> = persistentListOf(),
    val connections: ImmutableMap<String, ImmutableList<String>> = persistentMapOf(),
    val zoom: Float = 1f,
    val graphUserPosition: Offset = Offset.Zero,
    val graphViewSettings: GraphViewSettings = GraphViewSettings.Default.copy(targetFrameMs = 1L),
    val coordinates: ImmutableMap<String, Offset> = persistentMapOf(),
    val velocities: ImmutableMap<String, Offset> = persistentMapOf(),
) {
    @Serializable
    data class SaveableState(
        val isShowSettings: Boolean = false,
        val config: GraphSettingsConfig = GraphSettingsConfig.Default,
        val graphNodes: List<ObsidianGraphNode> = listOf(),
        val connections: Map<String, List<String>> = mapOf(),
        val zoom: Float = 1f,
        val graphUserPosition: OffsetData = OffsetData.Zero,
        val graphViewSettings: GraphViewSettings = GraphViewSettings.Default,
        val coordinates: Map<String, OffsetData> = mapOf()
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                isShowSettings = isShowSettings,
                config = config,
                graphNodes = graphNodes.toPersistentList(),
                connections = connections
                    .mapValues { it.value.toPersistentList() }
                    .toPersistentMap(),
                zoom = zoom,
                graphUserPosition = graphUserPosition.mapToOffset(),
                graphViewSettings = graphViewSettings,
                coordinates = coordinates
                    .mapValues { it.value.mapToOffset() }
                    .toPersistentMap()
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                isShowSettings = isShowSettings,
                config = config,
                graphNodes = graphNodes,
                connections = connections,
                zoom = zoom,
                graphUserPosition = graphUserPosition.mapToOffsetData(),
                graphViewSettings = graphViewSettings,
                coordinates = coordinates
                    .mapValues { d -> d.value.mapToOffsetData() }
                    .toPersistentMap()
            )
        }
    }
}