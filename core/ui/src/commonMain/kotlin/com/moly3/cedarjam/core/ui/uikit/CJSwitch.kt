package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

@Composable
fun CJSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val appTheme = LocalAppTheme.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(checkedThumbColor = appTheme.primaryColor)
    )
}