package com.moly3.cedarjam.pages.page_home.func

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import com.moly3.lazyflow.core.model.FlowItemSize

fun DpSize.toPx(density: Density): FlowItemSize {
    val dpSize = this
    return FlowItemSize(
        widthPx = with(density) { dpSize.width.roundToPx() },
        heightPx = with(density) { dpSize.height.roundToPx() }
    )
}