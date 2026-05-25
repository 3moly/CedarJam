package com.moly3.cedarjam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogGraphConfigsService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectOptionsService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.dialog.model.DialogSelectOptionsServiceInput
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalVideoPlayer
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJApplicationTheme
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.pages.page_select_workspace.ui.SelectWorkspacePage
import com.moly3.cedarjam.pages.page_workspace.ui.WorkspacePage
import com.moly3.cedarjam.pages.page_workspace.ui.dialog.DialogSelectTagUI
import com.moly3.cedarjam.pages.page_workspace.ui.dialog.DialogTagToTagUI
import com.moly3.cedarjam.ui.app.AppComposableWidgetHideKeyboard
import com.moly3.cedarjam.ui.dialog.DialogColorPickerUI
import com.moly3.cedarjam.ui.dialog.DialogCreateWorkspaceUI
import com.moly3.cedarjam.ui.dialog.DialogDeleteUI
import com.moly3.cedarjam.ui.dialog.DialogSelectOptionsUI
import com.moly3.cedarjam.ui.dialog.SuccessSnackbarComponent
import com.moly3.cedarjam.di.metro.CedarJamGraph
import com.moly3.cedarjam.ui.dialog.DialogGraphConfigsUI
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun MainApp(root: Root) {
    val registry = remember {
        root.dialogRegistry.apply {
            registerUI { dialog: DialogSelectOptionsService, input: DialogSelectOptionsServiceInput ->
                DialogSelectOptionsUI(dialog, input)
            }
            registerUI { dialog: DialogColorPickerService, input: Color? ->
                DialogColorPickerUI(dialog, input ?: Color.Black)
            }
            registerUI { dialog: DialogCreateWorkspaceService, input: Unit ->
                DialogCreateWorkspaceUI(dialog)
            }
            registerUI { dialog: DialogGraphConfigsService, input: DialogGraphConfigsService.Input ->
                DialogGraphConfigsUI(dialog = dialog, input = input)
            }
            registerUI { dialog: DialogDeleteService, input: Unit ->
                DialogDeleteUI(dialog)
            }
            registerUI { dialog: DialogSelectTagService, i ->
                DialogSelectTagUI(
                    dialog = dialog,
                    workspaceSession = i
                )
            }
            registerUI { dialog: DialogTagToTagService, i ->
                DialogTagToTagUI(
                    dialog = dialog,
                    workspaceSession = i
                )
            }
        }
    }

    val stack by root.children.subscribeAsState()
    val appSettings by root.appSettingsFlow.collectAsState()
    val syncStatus by root.sendingBranchFlow.collectAsState(UIState.Loading)
    CJApplicationTheme(isRelease = root.isRelease, appSettings = appSettings) {
        val playerState = rememberVideoPlayerState()
        CompositionLocalProvider(
            LocalVideoPlayer provides playerState,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalAppTheme.current.colors.backgroundPrimary)
            ) {
                ChildStack(
                    modifier = Modifier
                        .fillMaxSize()
                        .onPointerEvent(PointerEventType.Press) {
                            if (it.buttons.isBackPressed) {
                                root.onNavigate(Route.Back)
                            } else if (it.buttons.isForwardPressed) {
                                root.onNavigate(Route.Forward)
                            }
                        },
                    stack = stack,
                    animation = stackAnimation()
                ) {
                    when (val instance = it.instance) {
                        is Root.Child.Workspace -> WorkspacePage(
                            component = instance.component,
                            dialogContent = {
                                registry.Host()
                            }
                        )

                        is Root.Child.SelectWorkspace -> {
                            SelectWorkspacePage(component = instance.component)
                            registry.Host()
                        }
                    }
                }
                Box(Modifier.fillMaxSize()) {
                    SuccessSnackbarComponent(
                        messageService = root.messageService
                    )
                }
                AppComposableWidgetHideKeyboard()
                TopAlertServiceUI(root.alertService)
                when (val sync = syncStatus) {
                    is UIState.Error,
                    UIState.Loading -> {
                    }

                    is UIState.Success -> {
                        NeumorphicShape(
                            modifier = Modifier.align(Alignment.Center).size(200.dp),
                            content = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        com.moly3.cedarjam.core.ui.uikit.CJText(
                                            text = sync.data.message
                                        )
                                        com.moly3.cedarjam.core.ui.uikit.CJText(
                                            text = "progress: ${sync.data.progress}/${sync.data.all}"
                                        )
                                    }
                                }
                            }) {

                        }
                    }
                }
            }
        }
    }
}
