package com.moly3.cedarjam.core.domain.dialog.model

import com.moly3.cedarjam.core.domain.model.Workspace

data class DialogSelectWorkspaceServiceInput(
    val activeWorkspace: Workspace?,
)