package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.dialog.model.DialogSelectWorkspaceServiceInput
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation

class DialogSelectWorkspaceService : GlobalDialog<DialogSelectWorkspaceServiceInput, WorkspacePresentation?>(
    closeValue = null
)