package com.moly3.cedarjam.features.feature_canvas

import androidx.compose.ui.geometry.Offset
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.canvas.ShapeImpl
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.dataviz.core.whiteboard.model.ShapeConnection

sealed interface Intent {
    data class MoveShape(val index: Int, val position: Offset) : Intent
    data class DeleteShape(val shape: ShapeImpl) : Intent
    data class ChangeShape(val shape: ShapeImpl) : Intent
    data class ResizeShape(val index: Int, val position: Offset, val size: Offset) :
        Intent

    data class SetZoom(val value: Float) : Intent
    data class SetUserCoordinate(val value: Offset) : Intent
    data object Close : Intent
    data object AddShape : Intent
    data class AddFileShape(val file: FileTreeNode) : Intent
    data class AddConnection(val arcConnection: ShapeConnection<Long>) : Intent
    data class SetIsShowContent(val value: Boolean) : Intent
    data class OpenNode(val value: ObsidianGraphData) : Intent
}