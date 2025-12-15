package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.layer.drawLayer

fun Modifier.blendMode(blendMode: BlendMode): Modifier = drawWithCache {
    val layer = obtainGraphicsLayer()
    layer.apply {
        record { drawContent() }
        this.blendMode = blendMode
    }
    onDrawWithContent { drawLayer(layer) }
}