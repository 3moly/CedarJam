package com.moly3.cedarjam.features.feature_graph

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.OffsetData
import com.moly3.cedarjam.core.domain.model.mapToOffset
import com.moly3.cedarjam.core.domain.model.mapToOffsetData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable

data class State(
    val isShowContent: Boolean = true,
    val graphNodes: ImmutableList<ObsidianGraphNode> = persistentListOf(),
    val connections: ImmutableMap<String, ImmutableList<String>> = persistentMapOf(),
    val coordinates: ImmutableMap<String, Offset> = persistentMapOf(),
    val velocities: ImmutableMap<String, Offset> = persistentMapOf(),
    val zoom: Float = 1f
) {
    @Serializable
    data class SaveableState(
        val isShowContent: Boolean,
        val zoom: Float,
        val graphNodes: List<ObsidianGraphNode>,
        val connections: Map<String, List<String>>,
        val coordinates: Map<String, OffsetData>
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                isShowContent = isShowContent,
                graphNodes = graphNodes.toPersistentList(),
                connections = connections
                    .mapValues { it.value.toPersistentList() }
                    .toPersistentMap(),
                zoom = zoom,
                coordinates = coordinates
                    .mapValues { it.value.mapToOffset() }
                    .toPersistentMap()
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                graphNodes = graphNodes,
                connections = connections,
                zoom = zoom,
                coordinates = coordinates
                    .mapValues { d -> d.value.mapToOffsetData() }
                    .toPersistentMap(),
                isShowContent = isShowContent
            )
        }
    }
}