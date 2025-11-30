package com.moly3.cedarjam.ui.pages.tag.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.moly3.cedarjam.features.feature_graph.ui.ContentNearGraphUI
import com.moly3.cedarjam.ui.pages.tag.Intent
import com.moly3.cedarjam.ui.pages.tag.TagComponent
import com.moly3.cedarjam.ui.pages.tag.ui.internal.PageContent

@Composable
fun TagPage(component: TagComponent) {
    val state = component.state.collectAsState().value
    ContentNearGraphUI(
        mainContent = { PageContent(state, onIntent = component::onIntent) },
        dialogSlot = component.dialogSlot,
        connectionsCount = state.connections.size,
        setIsShowGraph = { component.onIntent(Intent.SetIsShowGraph(it)) }
    )
}