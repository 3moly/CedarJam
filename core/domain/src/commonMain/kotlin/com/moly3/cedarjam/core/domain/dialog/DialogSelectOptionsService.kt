package com.moly3.cedarjam.core.domain.dialog

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.dialog.model.DialogSelectOptionsServiceInput

@Stable
class DialogSelectOptionsService(register: IDialogRegister) :
    GlobalDialog<DialogSelectOptionsServiceInput, Unit>(
        closeValue = Unit, register = register
    )