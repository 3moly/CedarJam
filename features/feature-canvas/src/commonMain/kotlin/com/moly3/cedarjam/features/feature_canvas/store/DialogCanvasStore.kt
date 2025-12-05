package com.moly3.cedarjam.features.feature_canvas.store

import androidx.compose.ui.geometry.Offset
import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.canvas.ShapeImpl
import com.moly3.cedarjam.features.feature_canvas.Intent
import com.moly3.cedarjam.features.feature_canvas.State
import com.moly3.dataviz.core.whiteboard.model.ShapeConnection
import kotlinx.collections.immutable.ImmutableList

internal interface DialogCanvasStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetShapes(val value: ImmutableList<ShapeImpl>) : Msg
        data class SetConnections(val value: ImmutableList<ShapeConnection<Long>>) : Msg
        data class SetZoom(val value: Float) : Msg
        data class SetWorkspaceFullpath(val value: String) : Msg
        data class SetUserCoordinate(val value: Offset) : Msg
        data class SetIsShowContent(val value: Boolean) : Msg
    }
}