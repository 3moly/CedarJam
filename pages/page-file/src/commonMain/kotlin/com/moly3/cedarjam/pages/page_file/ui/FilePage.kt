package com.moly3.cedarjam.pages.page_file.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.features.feature_canvas.ui.DialogCanvasUI
import com.moly3.cedarjam.features.feature_graph.ui.ContentNearGraphUI
import com.moly3.cedarjam.features.feature_file_view.FileView
import com.moly3.cedarjam.pages.page_file.FileComponent
import com.moly3.cedarjam.pages.page_file.Intent
import com.moly3.cedarjam.pages.page_file.ui.internal.PageContent

@Composable
fun FilePage(component: FileComponent) {
    val state = component.state.collectAsState().value
    ContentNearGraphUI(
        mainContent = {
            val dialogSlot by component.dialogCanvasSlot.subscribeAsState()
            val canvasComponent = dialogSlot.child?.instance
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
                            }
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
        },
        dialogSlot = component.dialogGraphSlot,
        connectionsCount = state.connectionsCount,
        setIsShowGraph = { component.onIntent(Intent.SetIsShowGraph(it)) },
        optionsAlignment = Alignment.BottomEnd
    )
}