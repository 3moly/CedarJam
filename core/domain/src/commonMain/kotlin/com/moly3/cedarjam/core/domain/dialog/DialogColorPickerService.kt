package com.moly3.cedarjam.core.domain.dialog

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
class DialogColorPickerService(register: IDialogRegister) :
    GlobalDialog<Color?, Color?>(closeValue = null, register = register)