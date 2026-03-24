package com.moly3.cedarjam.core.domain.dialog

import androidx.compose.runtime.Stable

@Stable
class DialogDeleteService : GlobalDialog<Unit, Boolean>(
    closeValue = false
)