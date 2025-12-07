package com.moly3.cedarjam.pages.page_select_workspace.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.pages.page_select_workspace.Intent
import com.moly3.cedarjam.pages.page_select_workspace.State

internal interface SelectWorkspaceStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetWorkspaces(val value: UIState<List<WorkspacePresentation>, Nothing>) : Msg
    }
}
