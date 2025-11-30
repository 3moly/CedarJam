package com.moly3.cedarjam.pages.page_collection.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.moly3.cedarjam.pages.page_collection.CollectionComponent
import com.moly3.cedarjam.pages.page_collection.ui.internal.PageContent

@Composable
fun CollectionPage(component: CollectionComponent) {
    val state = component.state.collectAsState().value
    PageContent(state, onIntent = component::onIntent)
}