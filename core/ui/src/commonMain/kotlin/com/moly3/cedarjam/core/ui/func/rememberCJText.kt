package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import com.moly3.cedarjam.core.ui.model.CJText
import org.jetbrains.compose.resources.stringResource

@Composable
fun CJText?.rememberCJText(): String {
    return   when (val name = this) {
        is CJText.Raw -> name.text
        is CJText.Res -> stringResource(name.res)
        null -> ""
    }
}