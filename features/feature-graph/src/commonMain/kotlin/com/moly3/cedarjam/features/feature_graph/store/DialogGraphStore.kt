package com.moly3.cedarjam.features.feature_graph.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ObsidianGraphNode
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.features.feature_graph.Intent
import com.moly3.cedarjam.features.feature_graph.State
import com.moly3.cedarjam.features.feature_graph.model.GraphPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

internal interface DialogGraphStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetRowsState(val value: UIState<ImmutableList<CollectionRowDTO>, Unit>) : Msg
        data class SetTagsState(val value: UIState<ImmutableList<TagDTO>, Unit>) : Msg
        data class SetFilesState(val value: UIState<ImmutableList<FileTreeNode>, Unit>) : Msg
        data class SetAnnotationsState(val value: UIState<ImmutableList<AnnotationDTO>, Unit>) : Msg
        data class SetNodes(val value: ImmutableList<ObsidianGraphNode>) : Msg
        data class SetCoordinates(val value: ImmutableMap<String, Offset>) : Msg
        data class SetVelocities(val value: ImmutableMap<String, Offset>) : Msg
        data class SetConnections(val value: ImmutableMap<String, ImmutableList<String>>) : Msg
        data class SetZoom(val value: Float) : Msg
        data class SetIsShowContent(val value: Boolean) : Msg
        data class SetAnnotationsScrollState(val value: Int) : Msg
        data class SetCurrentPage(val value: GraphPage) : Msg
        data class SetGraphTargetId(val value: String?) : Msg
        data class SetIsShowNestedConnections(val value: Boolean) : Msg
    }
}