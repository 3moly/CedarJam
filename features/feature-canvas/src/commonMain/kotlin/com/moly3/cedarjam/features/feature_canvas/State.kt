package com.moly3.cedarjam.features.feature_canvas

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.canvas.ShapeImpl
import com.moly3.dataviz.core.block.model.ShapeConnection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

data class State(
    val isShowContent: Boolean = true,
    val zoom: Float = 1f,
    val workspaceFullpath: String? = null,
    val userCoordinate: Offset = Offset.Zero,
    val shapes: ImmutableList<ShapeImpl> = persistentListOf(),
    val connections: ImmutableList<ShapeConnection> = persistentListOf(),
) {
    @Serializable
    data class SaveableState(
        val isShowContent: Boolean,
        val zoom: Float
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                isShowContent = isShowContent,
                zoom = zoom,
                connections = persistentListOf(),
                shapes = persistentListOf()
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                zoom = zoom,
                isShowContent = isShowContent
            )
        }
    }
}