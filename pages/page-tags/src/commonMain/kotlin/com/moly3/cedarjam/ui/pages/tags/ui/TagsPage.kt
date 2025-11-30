package com.moly3.cedarjam.ui.pages.tags.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.moly3.cedarjam.ui.pages.tags.TagsComponent
import com.moly3.cedarjam.ui.pages.tags.ui.internal.PageContent

@Composable
fun TagsPage(component: TagsComponent) {
    val state = component.state.collectAsState().value
    PageContent(state, onIntent = component::onIntent)
}