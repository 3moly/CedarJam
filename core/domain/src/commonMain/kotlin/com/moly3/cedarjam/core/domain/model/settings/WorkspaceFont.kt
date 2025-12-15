package com.moly3.cedarjam.core.domain.model.settings

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.model.FileTreeNode

@Stable
data class WorkspaceFont(
    val font: FileTreeNode.File,
    val timestamp: Long
)