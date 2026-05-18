package com.moly3.cedarjam.features.feature_settings.child.sync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJCircularProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJToolbar
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.features.feature_settings.child.SettingsContent
import com.moly3.cedarjam.features.feature_settings.child.sync.ISettingsSyncComponent
import com.moly3.cedarjam.features.feature_settings.child.sync.Intent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsSyncUI(component: ISettingsSyncComponent) {
    val state by component.state.collectAsState()
    SettingsContent {
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
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer progress ring (file progress)
                    when (val channel = state.uploadStateChannel) {
                        is UIState.Success -> {
                            if (channel.data.fileProgress != null && channel.data.fileProgress!! > 0f) {
                                CJCircularProgressIndicator(
                                    progress = channel.data.fileProgress,
                                    modifier = Modifier.size(150.dp),
//                                    color = LocalAppTheme.current.colors.primary.copy(alpha = 0.3f),
//                                    strokeWidth = 3.dp,
//                                    trackColor = Color.Transparent
                                )
                            }
                        }

                        else -> {}
                    }

                    // Inner progress ring (overall progress)
                    when (val channel = state.uploadStateChannel) {
                        is UIState.Success -> {
                            val overallProgress = if (channel.data.all > 0) {
                                channel.data.progress.toFloat() / channel.data.all.toFloat()
                            } else 0f

//                            CircularProgressIndicator(
//                                progress = overallProgress,
//                                modifier = Modifier.size(140.dp),
//                                color = LocalAppTheme.current.colors.primary,
//                                strokeWidth = 4.dp,
//                                trackColor = LocalAppTheme.current.colors.backgroundSecondary.copy(alpha = 0.3f)
//                            )
                            CJCircularProgressIndicator(
                                Modifier.size(140.dp),
                                progress = overallProgress
                            )
                        }

                        UIState.Loading -> {
                            CJCircularProgressIndicator(
                                Modifier.size(140.dp)
                            )
                        }

                        else -> {}
                    }

                    // Main button
                    Box(
                        Modifier
                            .size(130.dp)
                            .background(
                                LocalAppTheme.current.colors.backgroundSecondary,
                                shape = RoundedCornerShape(65.dp)
                            )
                            .clip(RoundedCornerShape(65.dp))
                            .let {
                                val isClickable = when (val channel = state.uploadStateChannel) {
                                    is UIState.Error,
                                    UIState.Loading -> true

                                    is UIState.Success -> channel.data.all == channel.data.progress
                                }
                                if (isClickable) {
                                    it.clickable {
                                        component.onIntent(Intent.Sync)
                                    }
                                } else {
                                    it
                                }
                            }
                            .border(
                                width = 2.dp,
                                color = LocalAppTheme.current.primaryColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(65.dp)
                            )
                    ) {
                        CJText(
                            text = "Sync",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                when (val channel = state.uploadStateChannel) {
                    is UIState.Error -> {
                        CJText(
                            text = channel.error,
                            color = Color.Red
                        )
                    }

                    UIState.Loading -> {
                        CJText(text = "Preparing...")
                    }

                    is UIState.Success -> {
                        CJText(
                            text = "${channel.data.message} ${channel.data.progress}/${channel.data.all}"
                        )
                    }
                }
                Column(
                    Modifier.weight(1f).align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    UIStateContentNoBox(state = state.fileVersionsState) {
                        if (it.toDownload.isNotEmpty()) {
                            CJText(text = "to download...", fontSize = 11.sp)
                            for (item in it.toDownload) {
                                Column {
                                    CJText(text = item.key, fontSize = 11.sp)
                                    CJText(text = item.value, fontSize = 11.sp)
                                }

                            }
                        }
                        if (it.toUpload.isNotEmpty()) {
                            CJText(text = "to upload...", fontSize = 11.sp)
                            for (item in it.toUpload) {
                                Column {
                                    CJText(text = item.key, fontSize = 11.sp)
                                    CJText(text = item.value, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

//            Column {
////                for (pair in state.deletedFiles) {
////                    CJText(text = pair.key, fontSize = 8.sp)
////                }
//            }
//            UIStateContentNoBox(state = state.fileVersionsState) {
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    CJText(text = "-> ${it.filesToArchive.size}")
//                    CJText(text = "<- ${it.filesToDownload.size}")
//                    CJText(text = "D local ${it.localDeletedFilesByServer.size}")
//                }
//            }
//            CJButton(text = "sync") {
//                component.onIntent(Intent.Sync)
//            }

//            UIStateContentNoBox(state = state.fileVersionsState) {
//                val headers: List<Header<FileVersionLine>> = remember(it) {
//                    listOf(
//                        Header(
//                            headerName = "status",
//                            content = { item ->
//                                val color = remember(item) {
//                                    val serverTime = item.serverTime
//                                    val currentTime = item.currentTime
//                                    if (serverTime == null && currentTime == null) {
//                                        Color.Gray
//                                    } else if (serverTime == null && currentTime != null) {
//                                        Color.Blue
//                                    } else if (currentTime == null && serverTime != null) {
//                                        Color.Red
//                                    } else if (currentTime!! > serverTime!!) {
//                                        Color.Blue
//                                    } else {
//                                        Color.Red
//                                    }
//                                }
//                                Box(Modifier.size(40.dp).background(color))
//                            }
//                        ),
//                        Header(
//                            headerName = "name",
//                            rowWidth = 100.dp,
//                            content = { item ->
//                                CJText(text = item.fileRelativePath, maxLines = 5)
//                            }
//                        ),
//                        Header(
//                            headerName = "status",
//                            content = { item ->
//                                val localText = if (item.currentTime == null)
//                                    ""
//                                else
//                                    "local: ${item.currentTime.formatEpochMillis()}"
//                                val serverText = if (item.serverTime == null)
//                                    ""
//                                else
//                                    "server: ${item.serverTime.formatEpochMillis()}"
//                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                                    CJText(
//                                        text = localText,
//                                        maxLines = 1
//                                    )
//                                    CJText(
//                                        text = "${item.currentTime}",
//                                        maxLines = 1
//                                    )
//                                    CJText(
//                                        text = serverText,
//                                        maxLines = 1
//                                    )
//                                    CJText(
//                                        text = "${item.serverTime}",
//                                        maxLines = 1
//                                    )
//                                    CJText(
//                                        text = if (item.serverTime != item.currentTime &&
//                                            item.serverTime != null &&
//                                            item.currentTime != null
//                                        ) {
//                                            (kotlin.math.abs(item.serverTime - item.currentTime)).toString()
//                                        } else "",
//                                        maxLines = 1
//                                    )
//                                }
//                            }
//                        ))
//                }
//                Column(Modifier) {
//                    CJDataTable(
//                        isLazyColumn = false,
//                        isFixedHeader = false,
//                        modifier = Modifier,
//                        headers = headers,
//                        data = it
//                    )
//                }
//            }
//            val headers: List<Header<IndexFileDto>> = remember(state.indexFiles) {
//                listOf(
//                    Header(
//                        headerName = "relativePath",
//                        contentStr = {
//                            it.relativePath
//                        }
//                    ),
//                    Header(
//                        headerName = "status",
//                        rowWidth = 100.dp,
//                        contentStr = {
//                            it.serverSyncStatus .toString()
//                        }
//                    ))
//            }
//            CJText(
//                modifier = Modifier,
//                text = "indexes: ${state.indexFiles.size}"
//            )
//            CJDataTable(
//                isLazyColumn = false,
//                isFixedHeader = false,
//                modifier = Modifier,
//                headers = headers,
//                data = state.indexFiles
//            )
//            UIStateContentNoBox(state = state.uploadState) {
//                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//
//                    CJText(
//                        modifier = Modifier,
//                        text = "filesToArchive: ${it.filesToArchive.size}"
//                    )
//                    for (item in it.filesToArchive) {
//                        CJText(text = item.toString())
//                    }
//                    CJText(
//                        modifier = Modifier.padding(top = 12.dp),
//                        text = "files Downloaded:   ${it.filesDownloaded.size}"
//                    )
//                    for (item in it.filesDownloaded) {
//                        CJText(text = item.toString())
//                    }
//                    CJText(
//                        modifier = Modifier.padding(top = 12.dp),
//                        text = "files To Download:  ${it.filesToDownload.size}"
//                    )
//                    for (item in it.filesToDownload) {
//                        CJText(text = item)
//                    }
//                    CJText(
//                        modifier = Modifier.padding(top = 12.dp),
//                        text = "local Deleted Files By Server: ${it.localDeletedFilesByServer.size}"
//                    )
//                    for (item in it.localDeletedFilesByServer) {
//                        CJText(text = item.getFullName())
//                    }
//                }
//            }
        }
    }
}