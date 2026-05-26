package com.moly3.cedarjam.features.feature_graph

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.OffsetData
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.mapToOffset
import com.moly3.cedarjam.core.domain.model.mapToOffsetData
import com.moly3.cedarjam.features.feature_graph.model.GraphPage
import com.moly3.dataviz.core.graph.model.Connection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable

data class State(
    val isShowContent: Boolean = true,
    val graphTargetId: String? = null,
    val currentPage: GraphPage = GraphPage.General,
    val tagsState: UIState<ImmutableList<TagDTO>, Unit> = UIState.Loading,
    val filesState: UIState<ImmutableList<FileTreeNode>, Unit> = UIState.Loading,
    val rowsState: UIState<ImmutableList<CollectionRowDTO>, Unit> = UIState.Loading,
    val annotationsState: UIState<ImmutableList<AnnotationDTO>, Unit> = UIState.Loading,
    val graphNodes: ImmutableList<ObsidianGraphNode> = persistentListOf(),
    val connections: ImmutableMap<String, ImmutableList<Connection<String>>> = persistentMapOf(),
    val coordinates: ImmutableMap<String, Offset> = persistentMapOf(),
    val velocities: ImmutableMap<String, Offset> = persistentMapOf(),
    val zoom: Float = 1f,
    val isShowNestedConnections: Boolean = false,
    val annotationsScrollState: Int = 0
) {
    @Serializable
    data class SaveableState(
        val isShowContent: Boolean,
        val currentPage: GraphPage,
        val zoom: Float,
        val graphNodes: List<ObsidianGraphNode>,
        val connections: Map<String, List<Connection<String>>>,
        val coordinates: Map<String, OffsetData>,
        val isShowNestedConnections: Boolean
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
                    .toPersistentMap(),
                currentPage = currentPage,
                isShowNestedConnections = isShowNestedConnections
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
                isShowContent = isShowContent,
                currentPage = currentPage,
                isShowNestedConnections = isShowNestedConnections
            )
        }
    }
}