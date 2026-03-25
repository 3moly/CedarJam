package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.service.WorkspaceSession

class DialogSelectTagService(register: IDialogRegister) : GlobalDialog<WorkspaceSession, TagDTO?>(
    isGenericDialog = false,
    closeValue = null,
    register = register
)