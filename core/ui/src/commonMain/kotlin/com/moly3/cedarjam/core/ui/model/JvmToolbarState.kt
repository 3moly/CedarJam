package com.moly3.cedarjam.core.ui.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class JvmToolbarState(
    val isFullscreen: Boolean,
    val modifier: Modifier,
    val isFirstCut: Boolean,
    val controlsWidthToCut: Dp
) {
    companion object {
        val Default = JvmToolbarState(
            isFullscreen = true,
            modifier = Modifier,
            isFirstCut = true,
            controlsWidthToCut = 0.dp
        )
    }
}