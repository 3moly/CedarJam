package com.moly3.cedarjam.pages.page_collection_row.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.moly3.cedarjam.features.feature_graph.ui.ContentNearGraphUI
import com.moly3.cedarjam.pages.page_collection_row.CollectionRowComponent
import com.moly3.cedarjam.pages.page_collection_row.Intent
import com.moly3.cedarjam.pages.page_collection_row.ui.internal.PageContent

@Composable
fun CollectionRowPage(component: CollectionRowComponent) {
    val state = component.state.collectAsState().value
    ContentNearGraphUI(
        mainContent = { PageContent(state, onIntent = component::onIntent) },
        dialogSlot = component.dialogSlot,
        connectionsCount = state.connectionsCount,
        setIsShowGraph = { component.onIntent(Intent.SetIsShowGraph(it)) }
    )
}