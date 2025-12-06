package com.moly3.cedarjam.core.domain.model.settings

import com.moly3.cedarjam.core.domain.model.FileTreeNode

data class WorkspaceFont(
    val font: FileTreeNode.File,
    val timestamp: Long
)