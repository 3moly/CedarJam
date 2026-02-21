package com.moly3.cedarjam.pages.page_home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.moly3.cedarjam.core.ui.uikit.JustMenuContent
import com.moly3.cedarjam.pages.page_home.HomeComponent
import com.moly3.cedarjam.pages.page_home.ui.internal.PageContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomePage(component: HomeComponent) {
    val scope = rememberCoroutineScope()
    val state = component.state.collectAsState().value
    PageContent(
        workspaceFullPath = component.workspaceFullPath,
        state = state,
        onIntent = {
            scope.launch(Dispatchers.Main.immediate) {
                component.onIntent(it)
            }
        })
}