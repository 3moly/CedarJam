package com.moly3.cedarjam.pages.page_workspace.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.imePaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJWorkspaceTheme
import com.moly3.cedarjam.features.feature_settings.ui.DialogSettingsUI
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.pages.page_workspace.ui.internal.WorkspacePageContent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WorkspacePage(
    component: WorkspaceComponent,
    dialogContent: @Composable () -> Unit = {}
) {
    val state by component.state.collectAsState()

    CJWorkspaceTheme(
        settings = state.settings,
        file = state.workspaceFont,
        workspaceFullPath = state.activeWorkspace?.absolutePath
    ) {
        Box(
            Modifier
                .background(LocalAppTheme.current.colors.backgroundPrimary)
                .imePaddingCJ()
        ) {
            WorkspacePageContent(
                component = component,
                state = state,
                onIntent = { component.onIntent(it) }
            )
        }

        val child by component.settingsDialogSlot.subscribeAsState()
        AnimatedVisibility(
            visible = child.child != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            child.child?.instance?.let {
                DialogSettingsUI(it)
            }
        }
        dialogContent()
    }
}