package com.moly3.cedarjam.core.ui.compositions

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalUIConfig = compositionLocalOf { UIConfig() }

data class UIConfig(
    val fabCircleSize: Dp = 48.dp
)