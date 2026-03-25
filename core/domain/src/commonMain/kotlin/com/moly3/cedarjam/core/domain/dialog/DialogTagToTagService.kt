package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.dialog.model.DialogTagToTagServiceData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession

class DialogTagToTagService(register: IDialogRegister) :
    GlobalDialog<WorkspaceSession, DialogTagToTagServiceData?>(
        isGenericDialog = false,
        closeValue = null,
        register = register
    )