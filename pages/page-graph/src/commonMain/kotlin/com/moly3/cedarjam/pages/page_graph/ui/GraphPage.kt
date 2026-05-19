package com.moly3.cedarjam.pages.page_graph.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.moly3.cedarjam.pages.page_graph.GraphComponent
import com.moly3.cedarjam.pages.page_graph.ui.internal.PageContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GraphPage(component: GraphComponent) {
    val state = component.state.collectAsState().value
    val scope = rememberCoroutineScope()
    PageContent(
        state = state,
        engine = component.engine,
        onIntent = {
            scope.launch(Dispatchers.Main) {
                component.onIntent(it)
            }
        })
}