package com.moly3.cedarjam.core.ui.model

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class JvmToolbarState(
    val isFullscreen: Boolean,
    val modifier: Modifier,
    val isFirstCut: Boolean,
    val endControlsWidth: Dp,
    val controlsWidthToCut: Dp
) {
    companion object {
        val Default = JvmToolbarState(
            isFullscreen = true,
            modifier = Modifier,
            isFirstCut = true,
            endControlsWidth = 0.dp,
            controlsWidthToCut = 0.dp
        )
    }
}