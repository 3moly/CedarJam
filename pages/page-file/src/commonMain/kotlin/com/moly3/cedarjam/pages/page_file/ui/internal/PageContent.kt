package com.moly3.cedarjam.pages.page_file.ui.internal

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.TagLinkDtoData
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.features.feature_file.FileEdit
import com.moly3.cedarjam.features.feature_file_view.FileView
import com.moly3.cedarjam.pages.page_file.Intent
import com.moly3.cedarjam.pages.page_file.State
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
internal fun PageContent(
    macTrackpadGestureService: MacTrackpadGestureService,
    workspaceSession: WorkspaceSession,
    filesRepository: IFilesRepository,
    utilsService: IUtilsService,
    jvmBrowserService: IJvmBrowserService,
    state: State,
    onIntent: (Intent) -> Unit
) {
    var isShow by remember { mutableStateOf(false) }
    val animateWidth by animateDpAsState(if (isShow) 300.dp else 0.dp)

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                if (state.fileType != null) {
                    FileView(
                        macTrackpadGestureService = macTrackpadGestureService,
                        jvmBrowserService = jvmBrowserService,
                        utilsService = utilsService,
                        filesRepository = filesRepository,
                        workspaceSession = workspaceSession,
                        modifier = Modifier,
                        fileNode = state.fileType,
                        contentFileEdit = { fileType ->
                            FileEdit(
                                modifier = Modifier.wstatusBarsPaddingCJ(),
                                text = fileType.value,
                                isCompact = false,
                                onSave = {
                                    onIntent(Intent.ChangeTextNode(fileType, it))
                                }
                            )
                        },
                        contentCanvas = { fileType -> },
                        backPage = {
                            onIntent(Intent.PageBack(it))
                        },
                        nextPage = {
                            onIntent(Intent.PageNext(it))
                        },
                        toPage = { pdf, page ->
                            onIntent(Intent.ToPage(pdf, page))
                        },
                        onAddAnnotation = {
                            onIntent(Intent.AddAnnotation(it))
                        },
                        onDeleteAnnotation = {
                            onIntent(Intent.DeleteAnnotation(it))
                        },
                        annotations = state.annotations
                    )
                }
            }
        }
        Column(Modifier.width(animateWidth)) {
            Row {
                for (item in state.tags) {
                    val tagLink = remember(item, state.tagLinks, state.relativePath) {
                        state.tagLinks.filter { d -> d.tagId == item.id }
                            .firstOrNull {
                                when (val d = it.data) {
                                    is TagLinkDtoData.FileNode -> d.relativePath == state.relativePath
                                }
                            }
                    }
                    CJButton(
                        modifier = Modifier,
                        text = item.name,
                        backColor = if (tagLink != null) Color.Blue else Color.Black
                    ) {
                        if (tagLink != null) {
                            onIntent(Intent.RemoveLinkTag(tagLink))
                        } else {
                            onIntent(Intent.SetLinkTag(item))
                        }

                    }
                }

            }
        }
    }
}