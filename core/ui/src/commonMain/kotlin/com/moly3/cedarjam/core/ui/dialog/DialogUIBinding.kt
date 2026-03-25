package com.moly3.cedarjam.core.ui.dialog

import androidx.compose.runtime.Composable
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog

class DialogUIBinding<Service : GlobalDialog<Input, Result>, Input, Result>(
    val service: Service,
    val ui: @Composable (Service, Input) -> Unit
)