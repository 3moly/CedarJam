package com.moly3.cedarjam.pages.page_file.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.features.feature_canvas.ui.DialogCanvasUI
import com.moly3.cedarjam.features.feature_file_view.FileView
import com.moly3.cedarjam.features.feature_graph.ui.ContentNearGraphUI
import com.moly3.cedarjam.pages.page_file.FileComponent
import com.moly3.cedarjam.pages.page_file.Intent
import com.moly3.cedarjam.pages.page_file.ui.internal.PageContent
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.persistentListOf

@Composable
fun FilePage(component: FileComponent) {
    val state by component.state.collectAsState()
    var isPressed by remember { mutableStateOf(false) }
    var isIOPressed by remember { mutableStateOf(false) }

    val appTheme = LocalAppTheme.current
    val hazeState = rememberHazeState(blurEnabled = true)
    val hazeStyle = remember(appTheme) {
        HazeStyle(
            backgroundColor = appTheme.colors.backgroundSecondary.copy(alpha = 0.8f),
            tints = listOf(HazeTint(appTheme.colors.backgroundSecondary.copy(0.5f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    ContentNearGraphUI(
        mainContent = {
            val dialogSlot by component.dialogCanvasSlot.subscribeAsState()
            val canvasComponent = dialogSlot.child?.instance
            Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
                if (canvasComponent != null) {
                    DialogCanvasUI(
                        modifier = Modifier,
                        component = canvasComponent,
                        onFileTypeView = {
                            FileView(
                                macTrackpadGestureService = component.macTrackpadGestureService,
                                filesRepository = component.filesRepository,
                                utilsService = component.utilsService,
                                jvmBrowserService = component.jvmBrowserService,
                                workspaceSession = component.workspaceSession,
                                modifier = Modifier,
                                fileNode = it,
                                contentFileEdit = { fileType -> },
                                contentCanvas = { fileType -> },
                                backPage = {
//                                onIntent(Intent.PageBack(it))
                                },
                                nextPage = {
//                                onIntent(Intent.PageNext(it))
                                },
                                toPage = { pdf, page ->
//                                onIntent(Intent.ToPage(pdf, page))
                                },
                                annotations = persistentListOf(),
                                onAddAnnotation = {},
                                onDeleteAnnotation = {}
                            )
                        }
                    )
                } else {
                    PageContent(
                        macTrackpadGestureService = component.macTrackpadGestureService,
                        filesRepository = component.filesRepository,
                        utilsService = component.utilsService,
                        jvmBrowserService = component.jvmBrowserService,
                        workspaceSession = component.workspaceSession,
                        state = state,
                        onIntent = component::onIntent
                    )
                }
            }
//            FileMenuContent(
//                modifier = Modifier.safeDrawingPadding().fillMaxSize(),
//                borderModifier = Modifier
//                    .clip(RoundedCornerShape(16.dp))
//                    .hazeEffect(hazeState, hazeStyle),
//                annotationsCount = state.annotations.size,
//                isIOSwitchPressed = isIOPressed,
//                isOpenedMenu = isPressed,
//                openWorkspaceSettings = {
//                    component.onIntent(Intent.OpenWorkspaceSettings)
//                },
//                onIOClick = {
//                    isIOPressed = !isIOPressed
//                },
//                onClick = {
//                    isPressed = !isPressed
//                }
//            )

        },
        dialogSlot = component.dialogGraphSlot,
        connectionsCount = state.connectionsCount,
        setIsShowGraph = { component.onIntent(Intent.SetIsShowGraph(it)) },
        optionsAlignment = Alignment.BottomEnd
    )
}