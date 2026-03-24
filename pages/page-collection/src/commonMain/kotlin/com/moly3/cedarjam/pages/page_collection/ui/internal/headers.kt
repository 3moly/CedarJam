package com.moly3.cedarjam.pages.page_collection.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.ui.compositions.LocalImageLoader
import com.moly3.cedarjam.core.ui.func.rememberPdfImage
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.Header
import vector.ArrowRight

fun imgHeader(
    workspace: WorkspacePresentation?,
    openDocument: (String) -> Unit
): Header<CollectionRowPresentation> {
    return Header(
        headerName = "img",
        rowWidth = 150.dp,
        content = {
            val fileRelativePath = it.row.fileRelativePath
            if (fileRelativePath != null) {
                val imgBitmap = rememberPdfImage(
                    workspaceFullPath = workspace?.absolutePath,
                    pathWrapper(
                        workspace?.absolutePath ?: "",
                        fileRelativePath
                    ).pathString
                )
                if (imgBitmap != null) {
                    Box(Modifier.height(200.dp)) {
                        AsyncImage(
                            model = imgBitmap!!,
                            contentDescription = null,
                            modifier = Modifier.height(200.dp),
                            contentScale = ContentScale.FillHeight
                        )
                        CJIcon(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            painter = rememberVectorPainter(ArrowRight),
                            onClick = {
                                openDocument(fileRelativePath)
                            })
                    }
                } else {
                    Box(Modifier.height(200.dp)) {
                        CJText(text = it.row.fileRelativePath ?: "-file")
                    }
                }
            }
            if (it.row.webLink != null) {
                val youtubeLink =
                    remember(it.row.webLink) { getYoutubeThumbnailUrl(it.row.webLink) }
                if (youtubeLink != null) {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        painter = rememberAsyncImagePainter(
                            youtubeLink,
                            imageLoader = LocalImageLoader.current
                        ),
                        contentDescription = null
                    )
                }
            }
        }
    )
}