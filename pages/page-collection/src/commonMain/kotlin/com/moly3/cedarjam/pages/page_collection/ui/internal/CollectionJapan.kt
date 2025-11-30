package com.moly3.cedarjam.pages.page_collection.ui.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.func.getPdfImage
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.ui.components.FlowerCard
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

@Composable
internal fun CollectionJapan(
    rows: List<CollectionRowDTO>,
    isPdfShow: Boolean,
    isOneWord: Boolean,
    youtubeLink: Boolean,
    webLink: Boolean,
    workspace: WorkspacePresentation?,
    openWebLink: (String) -> Unit = {},
    openRow: (CollectionRowDTO) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(top = 24.dp).padding(horizontal = 24.dp).fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in rows) {
            var imgBitmap by remember {
                mutableStateOf<ImageBitmap?>(null)
            }
            if (isPdfShow) {
                LaunchedEffect(row.fileRelativePath, workspace) {
                    launch(io) {
                       try {
                           imgBitmap = if (row.fileRelativePath != null) {
                               getPdfImage(
                                   Path("workspace?.fullpath", row.fileRelativePath!!).toString(),
                                   page = 0,
                                   dpi = 100f
                               )
                           } else {
                               null
                           }
                       }catch (exc: Exception){}
                    }
                }
            }

            FlowerCard(
                modifier = Modifier,
                text = row.name,
                webLink = if (webLink) row.webLink else null,
                isOneWord = isOneWord,
                documentImage = imgBitmap,
                youtubeLink = if (youtubeLink) getYoutubeThumbnailUrl(row.webLink) else null,
                blurRadius = 3,
                currentProgress = 0,
                progressMax = 0,
                onWebClick = {
                    openWebLink(it)
                },
                onClick = {
                    openRow(row)
                })
        }
    }
}

internal fun getYoutubeThumbnailUrl(videoUrl: String?): String? {
    if (videoUrl.isNullOrEmpty())
        return null
    val regex = Regex(
        "(?:(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com|youtu\\.be)/(?:watch\\?v=|embed/|v/)?([\\w-]{11}))"
    )
    val matchResult = regex.find(videoUrl)
    val videoId = matchResult?.groupValues?.get(1)

    return videoId?.let { "https://img.youtube.com/vi/$it/maxresdefault.jpg" }
}