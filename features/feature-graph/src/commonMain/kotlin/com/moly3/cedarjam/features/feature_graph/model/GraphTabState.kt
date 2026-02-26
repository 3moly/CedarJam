package com.moly3.cedarjam.features.feature_graph.model

data class GraphTabState(
    val canGoBack: Boolean,
    val canGoForward: Boolean,
    val goBack: () -> Unit,
    val goForward: () -> Unit,
)