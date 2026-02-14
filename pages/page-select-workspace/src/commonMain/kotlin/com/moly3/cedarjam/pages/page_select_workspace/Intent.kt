package com.moly3.cedarjam.pages.page_select_workspace

import com.moly3.cedarjam.core.domain.model.WorkspacePresentation

sealed class Intent {
    data class SelectWorkspace(val workspace: WorkspacePresentation) : Intent()
    data class DeleteWorkspace(val workspace: WorkspacePresentation) : Intent()
    data object CreateWorkspace : Intent()
    data class FastCreate(val name: String) : Intent()
}