package com.moly3.cedarjam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.compositions.LocalVideoPlayer
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJApplicationTheme
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.pages.page_select_workspace.ui.SelectWorkspacePage
import com.moly3.cedarjam.pages.page_workspace.ui.WorkspacePage
import com.moly3.cedarjam.ui.app.AppComposableWidgetHideKeyboard
import com.moly3.cedarjam.ui.dialog.DialogAddCollectionRowUI
import com.moly3.cedarjam.ui.dialog.DialogColorPickerUI
import com.moly3.cedarjam.ui.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.ui.dialog.DialogDeleteUI
import com.moly3.cedarjam.ui.dialog.DialogSelectWorkspaceUI
import com.moly3.cedarjam.ui.dialog.SuccessSnackbarComponent
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun MainApp(root: Root) {
    val stack by root.children.subscribeAsState()
    val appSettings by root.appSettingsFlow.collectAsState()
    val syncStatus by root.sendingBranchFlow.collectAsState(UIState.Loading)
    CJApplicationTheme(isRelease = root.isRelease, appSettings = appSettings) {
        val hazeState = rememberHazeState(blurEnabled = false)
        val playerState = rememberVideoPlayerState()
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
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
                        .hazeSource(state = hazeState)
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
                        is Root.Child.Workspace -> WorkspacePage(component = instance.component)
                        is Root.Child.SelectWorkspace -> SelectWorkspacePage(component = instance.component)
                    }
                }
                Box(Modifier.fillMaxSize()) {
                    val player = LocalVideoPlayer.current
                    if (player.hasMedia) {
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .height(200.dp)
                                .padding(16.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            VideoPlayerSurface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd),
                                playerState = player
                            )
                            Row(
                                modifier = Modifier.padding(8.dp).align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val buttonText = if (player.isPlaying) "stop" else "play"
                                CJButton(modifier = Modifier, text = buttonText) {
                                    if (player.isPlaying) {
                                        player.stop()
                                    } else {
                                        player.play()
                                    }
                                }
                            }
                        }
                    }
                }
                DialogDeleteUI(root.dialogDeleteService)
                DialogAddCollectionRowUI(root.dialogAddCollectionRowService)
                DialogCreateWorkspaceService(root.dialogCreateWorkspaceService)
                DialogSelectWorkspaceUI(
                    appEnvironment = root.appEnvironment,
                    dialogCreate = root.dialogCreateWorkspaceService,
                    root.dialogSelectWorkspaceService
                )
                DialogColorPickerUI(dialog = root.dialogColorPickerService)

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
//                Box(Modifier.fillMaxSize().background(Color.Black).padding(40.dp).flatClickable {}){
//                    val editorState = rememberNotionEditorState(
//                        initial = listOf(
//                            NotionBlock(type = BlockType.H1, text = "My Document"),
//                            NotionBlock(type = BlockType.Paragraph, text = "Start writing here..."),
//                            NotionBlock(
//                                type = BlockType.Custom,
//                                customContent = {
//                                    // Any composable you like
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
//                                            .padding(12.dp),
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                                    ) {
//                                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2383E2))
//                                        Text("This is a custom block!", fontWeight = FontWeight.Medium)
//                                    }
//                                }
//                            ),
//                            NotionBlock(type = BlockType.Image), // placeholder until you supply an ImageBitmap
//                        )
//                    )
//
//                    NotionEditor(
//                        state = editorState,
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
            }
        }
    }
}
