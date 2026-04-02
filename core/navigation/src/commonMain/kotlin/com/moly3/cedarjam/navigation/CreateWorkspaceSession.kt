package com.moly3.cedarjam.navigation

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.service.WorkspaceSession

fun interface CreateWorkspaceSession {
    operator fun invoke(workspaceInput: WorkspaceInput, stateKeeper: StateKeeper): WorkspaceSession
}
