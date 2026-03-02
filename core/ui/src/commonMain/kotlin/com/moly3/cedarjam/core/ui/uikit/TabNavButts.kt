package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun TabNavButts(
    canGoBack: Boolean,
    canGoForward: Boolean,
    goBack: () -> Unit,
    goForward: () -> Unit
) {
    CJButtSnap(
        modifier = Modifier,
        painter = rememberVectorPainter(vectors.ArrowLeft),
        isSelected = !canGoBack,
        buttType = ButtSnapType.Start,
        onClick = goBack
    )
    CJButtSnap(
        modifier = Modifier,
        painter = rememberVectorPainter(vectors.ArrowRight),
        isSelected = !canGoForward,
        buttType = ButtSnapType.End,
        onClick = goForward
    )
}