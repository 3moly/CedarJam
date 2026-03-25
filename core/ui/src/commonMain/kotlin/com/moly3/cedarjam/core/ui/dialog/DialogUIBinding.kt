package com.moly3.cedarjam.core.ui.dialog

import androidx.compose.runtime.Composable
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog

class DialogUIBinding<Input, Result>(
    val service: GlobalDialog<Input, Result>,
    val ui: @Composable (GlobalDialog<Input, Result>, Input?) -> Unit
)