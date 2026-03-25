package com.moly3.cedarjam.core.domain.dialog

import androidx.compose.runtime.Stable

@Stable
class DialogDeleteService(register: IDialogRegister) : GlobalDialog<Unit, Boolean>(
    closeValue = false,
    register = register
)