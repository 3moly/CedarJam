package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.model.Workspace

class DialogCreateWorkspaceService(register: IDialogRegister) :
    GlobalDialog<Unit, Workspace?>(closeValue = null, register = register)