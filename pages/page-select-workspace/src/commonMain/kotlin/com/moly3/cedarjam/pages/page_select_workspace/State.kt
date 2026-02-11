package com.moly3.cedarjam.pages.page_select_workspace

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation

data class State(
    val workspacesState: UIState<List<WorkspacePresentation>, String> = UIState.Loading,
    val localWorkspacesState: UIState<List<FileTreeNode>, String> = UIState.Loading
)