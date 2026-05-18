package com.moly3.cedarjam.features.feature_graph.model

import androidx.compose.runtime.Stable

@Stable
data class GraphTabState(
    val canGoBack: Boolean,
    val canGoForward: Boolean
)