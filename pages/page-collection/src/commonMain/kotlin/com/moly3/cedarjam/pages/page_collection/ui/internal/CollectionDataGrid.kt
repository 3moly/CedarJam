package com.moly3.cedarjam.pages.page_collection.ui.internal

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.CollectionViewType
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalDragAndDrop
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJDataTable
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.Header
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable

@Serializable
data class CollectionRowPresentation(
    val isDragged: Boolean,
    val row: CollectionRowDTO,
    val tags: ImmutableList<TagCollectionRowDTO>
)

@Composable
internal fun CollectionDataGrid(
    modifier: Modifier,
    collection: CollectionDTO,
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
    val viewType = collection.viewType
    val dragAndDropState = LocalDragAndDrop.current
    val draggableItems = remember { mutableStateMapOf<Long, Boolean>() }
    val presentations = remember(rows, tagCollectionRows, draggableItems) {
        rows.map { row ->
            CollectionRowPresentation(
                isDragged = draggableItems[row.id] ?: false,
                row = row,
                tags = tagCollectionRows.filter {
                    row.id == it.rowId
                }.toPersistentList()
            )
        }.toPersistentList()
    }
    val headers: PersistentList<Header<CollectionRowPresentation>> =
        remember(presentations, viewType) {
            val lists = mutableListOf<Header<CollectionRowPresentation>>()
            if (viewType != CollectionViewType.Word) {
                lists.add(imgHeader(workspace = workspace, openDocument = {
                    openDocument(it)
                }))
            }
            lists.addAll(
                listOf(
                    Header(
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
                        headerName = "tags",
                        content = { row ->
                            val sorted = remember(row, tagCollectionRows) {
                                tagCollectionRows.filter { d -> d.rowId == row.row.id }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(
                                    rememberScrollState()
                                ), horizontalArrangement = Arrangement.spacedBy(8.dp)
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
//                            FlowRow(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//
//                            }
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
            )
            lists.toPersistentList()
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