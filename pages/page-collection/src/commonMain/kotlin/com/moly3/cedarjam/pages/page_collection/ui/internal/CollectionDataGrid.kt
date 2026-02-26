package com.moly3.cedarjam.pages.page_collection.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.util.Logger
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalDragAndDrop
import com.moly3.cedarjam.core.ui.compositions.LocalImageLoader
import com.moly3.cedarjam.core.ui.func.getPdfImage
import com.moly3.cedarjam.core.ui.func.rememberPdfImage
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJDataTable
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.Header
import vectors.ArrowRight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable

@Serializable
data class CollectionRowPresentation(
    val isDragged: Boolean,
    val row: CollectionRowDTO,
    val tags: List<TagCollectionRowDTO>
)

@Composable
internal fun CollectionDataGrid(
    modifier: Modifier,
    workspace: WorkspacePresentation?,
    rows: ImmutableList<CollectionRowDTO>,
    tags: ImmutableList<TagDTO>,
    tagCollectionRows: ImmutableList<TagCollectionRowDTO>,
    openRow: (CollectionRowDTO) -> Unit,
    addTag: (CollectionRowDTO) -> Unit,
    renameRow: (CollectionRowDTO, String) -> Unit,
    onSetDocument: (FileTreeItemPresentation, CollectionRowDTO) -> Unit,
    deleteRow: (CollectionRowDTO) -> Unit,
    openDocument: (String) -> Unit
) {
    val dragAndDropState = LocalDragAndDrop.current
    val draggableItems = remember { mutableStateMapOf<Long, Boolean>() }
    val presentations = remember(rows, tagCollectionRows, draggableItems) {
        rows.map { row ->
            CollectionRowPresentation(
                isDragged = draggableItems[row.id] ?: false,
                row = row,
                tags = tagCollectionRows.filter {
                    row.id == it.rowId
                }
            )
        }
    }
    val headers: PersistentList<Header<CollectionRowPresentation>> =
        remember(presentations) {
            persistentListOf(
                Header(
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
                ),

                Header(
                    rowWeight = 1f,
                    rowMinWidth = 100.dp,
                    headerName = "name",
                    contentStr = {
                        it.row.name
                    }
                ),
                Header(
                    rowWeight = 1f,
                    rowMinWidth = 100.dp,
                    headerName = "progress",
                    content = {
                        Column {
                            CJText(it.row.currentProgress.toString(), maxLines = 1)
                            CJText(it.row.progressMax.toString(), maxLines = 1)
                        }
                    }
                ),
                Header(
                    rowWidth = 150.dp,
                    headerName = "",
                    content = { row ->
                        val sorted = remember(row, tagCollectionRows) {
                            tagCollectionRows.filter { d -> d.rowId == row.row.id }
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (item in sorted) {
                                val tag = tags.firstOrNull { d -> d.id == item.tagId }
                                if (tag != null) {
                                    CJButton(
                                        modifier = Modifier.width(100.dp),
                                        text = tag.name,
                                        backColor = tag.color
                                    ) {
//                                    addTag(row)
                                    }
                                } else {
                                    CJButton(
                                        modifier = Modifier.width(100.dp),
                                        text = item.tagId.toString(),
                                        backColor = Color.Green
                                    ) {
//                                    addTag(row)
                                    }
                                }

                            }
                            CJButton(
                                modifier = Modifier.width(100.dp),
                                text = "add tag"
                            ) {
                                addTag(row.row)
                            }
                        }
                    }
                ),
                Header(
                    headerName = "",
                    content = { row ->
                        CJButton(
                            text = "open row"
                        ) {
                            openRow(row.row)
                        }
                    }
                ),
                Header(
                    headerName = "",
                    content = { row ->
                        CJButton(
                            text = "delete row"
                        ) {
                            deleteRow(row.row)
                        }
                    }
                ),
            )
        }
    val primaryColor = LocalAppTheme.current.primaryColor
    CJDataTable(
        modifier = modifier.fillMaxSize(),
        itemModifierBuilder = { data ->
            this.dropTarget(
                key = "targetKey: ${data.row.id}",
                state = dragAndDropState,
                onDragEnter = {
                    draggableItems[data.row.id] = true
                },
                onDragExit = {
                    draggableItems[data.row.id] = false
                },
                onDrop = { state ->
                    println("state: ${state}")
                    draggableItems[data.row.id] = false

                    //isDragTarget = false
                    onSetDocument(state.data, data.row)
                }
            ).let {
                if (draggableItems[data.row.id] == true)
                    it.border(3.dp, primaryColor)
                else
                    it
            }
        },
        headers = headers,
        data = presentations
    )
}