package com.moly3.cedarjam.pages.page_graph

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.OffsetData
import com.moly3.cedarjam.core.domain.model.config.GraphPartConfig
import com.moly3.cedarjam.core.domain.model.mapToOffset
import com.moly3.cedarjam.core.domain.model.mapToOffsetData
import com.moly3.dataviz.core.graph.model.Connection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable

data class State(
    val isShowSettings: Boolean = false,
    val graphNodes: ImmutableList<ObsidianGraphNode> = persistentListOf(),
    val connections: ImmutableMap<String, ImmutableList<Connection<String>>> = persistentMapOf(),
    val zoom: Float = 1f,
    val graphUserPosition: Offset = Offset.Zero,
    val coordinates: ImmutableMap<String, Offset> = persistentMapOf(),

    val partConfig: GraphPartConfig = GraphPartConfig.Default,
    val nodeLands: ImmutableMap<String, ImmutableList<String>> = persistentMapOf()
) {
    @Serializable
    data class SaveableState(
        val isShowSettings: Boolean = false,
        val graphNodes: List<ObsidianGraphNode> = listOf(),
        val connections: Map<String, List<Connection<String>>> = mapOf(),
        val zoom: Float = 1f,
        val graphUserPosition: OffsetData = OffsetData.Zero,
        val coordinates: Map<String, OffsetData> = mapOf(),
        val partConfig: GraphPartConfig = GraphPartConfig.Default
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                isShowSettings = isShowSettings,
                graphNodes = graphNodes.toPersistentList(),
                connections = connections
                    .mapValues { it.value.toPersistentList() }
                    .toPersistentMap(),
                zoom = zoom,
                graphUserPosition = graphUserPosition.mapToOffset(),
                coordinates = coordinates
                    .mapValues { it.value.mapToOffset() }
                    .toPersistentMap(),
                partConfig = partConfig
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                isShowSettings = isShowSettings,
                graphNodes = graphNodes,
                connections = connections,
                zoom = zoom,
                graphUserPosition = graphUserPosition.mapToOffsetData(),
                coordinates = coordinates
                    .mapValues { d -> d.value.mapToOffsetData() }
                    .toPersistentMap(),
                partConfig = partConfig
            )
        }
    }
}