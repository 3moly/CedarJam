package com.moly3.cedarjam.pages.page_workspace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJWorkspaceTheme
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.ui.internal.WorkspacePageContent
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlin.time.ExperimentalTime

const val ToolbarHeight = 40f

data class ToolbarState(
    val isFullscreen: Boolean,
    val menuButtonsWidth: Dp,
    val isFirstCut: Boolean,
    val controlsWidthToCut: Dp
)

@OptIn(ExperimentalTime::class)
@Composable
@TraceRecomposition
fun WorkspacePage(
    component: WorkspaceComponent,
    titleBarContent: @Composable (@Composable (ToolbarState) -> Unit) -> Unit = {}
) {
    val state by component.state.collectAsState()

    CJWorkspaceTheme(settings = state.settings, file = state.workspaceFont) {
        Box(
            Modifier
                .background(LocalAppTheme.current.colors.backgroundPrimary)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            WorkspacePageContent(
                component = component,
                state = state,
                onIntent = {
                    component.onIntent(it)
                },
                titleBarContent = titleBarContent
            )
        }
    }
}