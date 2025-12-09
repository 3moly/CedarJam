package com.moly3.cedarjam.features.feature_settings.child.sync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.func.formatEpochMillis
import com.moly3.cedarjam.core.domain.func.formatFileSize
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJDataTable
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJToolbar
import com.moly3.cedarjam.core.ui.uikit.Header
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import com.moly3.cedarjam.features.feature_settings.child.SettingsContent
import com.moly3.cedarjam.features.feature_settings.child.sync.Intent
import com.moly3.cedarjam.features.feature_settings.child.sync.ISettingsSyncComponent
import com.moly3.cedarjam.features.feature_settings.child.sync.model.FileVersionLine
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun JvmWindowScope.SettingsSyncUI(component: ISettingsSyncComponent) {
    val state by component.state.collectAsState()
    SettingsContent {
        Box {
            CJDraggableArea {
                CJToolbar(
                    title = stringResource(Res.string.f_settings_general_title),
                    onBack = {
                        component.onIntent(Intent.Back)
                    },
                    onClose = {
                        component.onIntent(Intent.Close)
                    }
                )
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(12.dp).verticalScroll(rememberScrollState())
        ) {
            Column {
                for (pair in state.deletedFiles) {
                    CJText(text = pair.key, fontSize = 8.sp)
                }
            }
            CJButton(text = "sync") {
                component.onIntent(Intent.Sync)
            }
            UIStateContentNoBox(state = state.fileVersionsState) {
                val headers: List<Header<FileVersionLine>> = remember(it) {
                    listOf(
                        Header(
                            headerName = "status",
                            content = { item ->
                                val color = remember(item) {
                                    val serverTime = item.serverTime
                                    val currentTime = item.currentTime
                                    if (serverTime == null && currentTime == null) {
                                        Color.Gray
                                    } else if (serverTime == null && currentTime != null) {
                                        Color.Blue
                                    } else if (currentTime == null && serverTime != null) {
                                        Color.Red
                                    } else if (currentTime!! > serverTime!!) {
                                        Color.Blue
                                    } else {
                                        Color.Red
                                    }
                                }
                                Box(Modifier.size(40.dp).background(color))
                            }
                        ),
                        Header(
                            headerName = "name",
                            rowWidth = 100.dp,
                            content = { item ->
                                CJText(text = item.fileRelativePath, maxLines = 5)
                            }
                        ),
                        Header(
                            headerName = "status",
                            content = { item ->
                                val localText = if (item.currentTime == null)
                                    ""
                                else
                                    "local: ${item.currentTime.formatEpochMillis()}"
                                val serverText = if (item.serverTime == null)
                                    ""
                                else
                                    "server: ${item.serverTime.formatEpochMillis()}"
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    CJText(
                                        text = localText,
                                        maxLines = 1
                                    )
                                    CJText(
                                        text = "${item.currentTime}",
                                        maxLines = 1
                                    )
                                    CJText(
                                        text = serverText,
                                        maxLines = 1
                                    )
                                    CJText(
                                        text = "${item.serverTime}",
                                        maxLines = 1
                                    )
                                    CJText(
                                        text = if (item.serverTime != item.currentTime &&
                                            item.serverTime != null &&
                                            item.currentTime != null
                                        ) {
                                            (kotlin.math.abs(item.serverTime - item.currentTime)).toString()
                                        } else "",
                                        maxLines = 1
                                    )
                                }
                            }
                        ))
                }
                Column(Modifier) {
                    CJDataTable(
                        isLazyColumn = false,
                        isFixedHeader = false,
                        modifier = Modifier,
                        headers = headers,
                        data = it
                    )
                }
            }
            UIStateContentNoBox(state = state.uploadState) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CJText(
                        modifier = Modifier,
                        text = "filesToArchive: ${it.filesToArchive.size}"
                    )
                    for (item in it.filesToArchive) {
                        CJText(text = item.toString())
                    }
                    CJText(
                        modifier = Modifier.padding(top = 12.dp),
                        text = "files Downloaded:   ${it.filesDownloaded.size}"
                    )
                    for (item in it.filesDownloaded) {
                        CJText(text = item.toString())
                    }
                    CJText(
                        modifier = Modifier.padding(top = 12.dp),
                        text = "files To Download:  ${it.filesToDownload.size}"
                    )
                    for (item in it.filesToDownload) {
                        CJText(text = item)
                    }
                    CJText(
                        modifier = Modifier.padding(top = 12.dp),
                        text = "local Deleted Files By Server: ${it.localDeletedFilesByServer.size}"
                    )
                    for (item in it.localDeletedFilesByServer) {
                        CJText(text = item.getFullName())
                    }
                }
            }
        }
    }
}