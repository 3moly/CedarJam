package com.moly3.cedarjam.features.feature_canvas.model

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import kotlinx.serialization.Serializable

@Serializable
data class DialogConfig(
    val file: FileTreeNode.File
)