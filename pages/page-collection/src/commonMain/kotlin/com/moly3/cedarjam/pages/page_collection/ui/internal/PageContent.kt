package com.moly3.cedarjam.pages.page_collection.ui.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.pages.page_collection.Intent
import com.moly3.cedarjam.pages.page_collection.State
import com.moly3.cedarjam.core.domain.model.CollectionViewType
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJButtSnap
import com.moly3.cedarjam.core.ui.uikit.ButtSnapType
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.core.ui.vectors.Data
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
internal fun PageContent(
    state: State,
    onIntent: (Intent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (state.collection != null) {
                        when (state.collection.viewType) {
                            CollectionViewType.DataGrid -> {
                                CollectionDataGrid(
                                    workspace = state.workspace,
                                    rows = state.rows,
                                    tags = state.tags,
                                    tagCollectionRows = state.tagCollectionRows,
                                    openRow = {
                                        onIntent(
                                            Intent.OpenCollectionRow(
                                                collectionId = it.collectionId,
                                                it.id
                                            )
                                        )
                                    },
                                    renameRow = { row, name ->
                                        onIntent(Intent.RenameCollectionRow(row, name))
                                    },
                                    addTag = {
                                        onIntent(Intent.AddCollectionRowTag(it))
                                    },
                                    deleteRow = {
                                        onIntent(Intent.DeleteCollectionRow(it.id))
                                    },
                                    onSetDocument = { file, row ->
                                        onIntent(Intent.SetDocumentToRow(row, file))
                                    },
                                    openDocument = {
                                        onIntent(Intent.OpenDocument(it))
                                    }
                                )
                            }

                            CollectionViewType.Youtube -> {
                                CollectionJapan(
                                    rows = state.rows,
                                    isOneWord = false,
                                    isPdfShow = false,
                                    youtubeLink = true,
                                    workspace = state.workspace,
                                    webLink = true,
                                    openWebLink = {
                                        onIntent(Intent.OpenWebLink(it))
                                    },
                                    openRow = { row ->
                                        onIntent(
                                            Intent.OpenCollectionRow(
                                                collectionId = row.collectionId,
                                                row.id
                                            )
                                        )
                                    }
                                )
                            }

                            CollectionViewType.PDF -> {
                                CollectionJapan(
                                    rows = state.rows,
                                    isOneWord = false,
                                    isPdfShow = true,
                                    workspace = state.workspace,
                                    youtubeLink = false,
                                    webLink = false,
                                    openRow = { row ->
                                        onIntent(Intent.OpenCollectionRow(row.collectionId, row.id))
                                    }
                                )
                            }

                            CollectionViewType.Anime -> {
                                CJText("Anime")
                            }

                            CollectionViewType.Japan -> {
                                CollectionJapan(
                                    rows = state.rows,
                                    isOneWord = true,
                                    isPdfShow = false,
                                    youtubeLink = false,
                                    workspace = state.workspace,
                                    webLink = false,
                                    openRow = { row ->
                                        onIntent(Intent.OpenCollectionRow(row.collectionId, row.id))
                                    }
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CJButton(text = "<") {
                        onIntent(Intent.PreviousPage)
                    }
                    CJText(text = state.currentPage.toString())
                    CJText(text = " - ")
                    CJText(text = state.maxPage.toString())
                    CJButton(text = ">") {
                        onIntent(Intent.NextPage)
                    }
                }
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var rowTextState by remember { mutableStateOf(TextFieldValue()) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.collection != null) {
                            Row {
                                CJButtSnap(
                                    painter = rememberVectorPainter(Data),
                                    isSelected = state.collection.viewType == CollectionViewType.DataGrid,
                                    buttType = ButtSnapType.Left
                                ) {
                                    onIntent(Intent.ChangeViewType(CollectionViewType.DataGrid))
                                }
                                CJButtSnap(
                                    painter = rememberVectorPainter(Data),
                                    isSelected = state.collection.viewType == CollectionViewType.Youtube,
                                    buttType = ButtSnapType.Center
                                ) {
                                    onIntent(Intent.ChangeViewType(CollectionViewType.Youtube))
                                }
                                CJButtSnap(
                                    painter = rememberVectorPainter(Data),
                                    isSelected = state.collection.viewType == CollectionViewType.PDF,
                                    buttType = ButtSnapType.Center
                                ) {
                                    onIntent(Intent.ChangeViewType(CollectionViewType.PDF))
                                }
//                                ButtSnap(
//                                    painter = painterResource(MR.images.img_flower),
//                                    isSelected = state.collection.viewType == CollectionViewType.Anime,
//                                    buttType = ButtSnapType.Center
//                                ) {
//                                    onIntent(Intent.ChangeViewType(CollectionViewType.Anime))
//                                }
                                CJButtSnap(
                                    painter = rememberVectorPainter(Data),
                                    isSelected = state.collection.viewType == CollectionViewType.Japan,
                                    buttType = ButtSnapType.Right
                                ) {
                                    onIntent(Intent.ChangeViewType(CollectionViewType.Japan))
                                }
                            }
                        }

                        CJTextField(
                            modifier = Modifier.weight(1f),
                            value = rowTextState,
                            onValueChanged = {
                                rowTextState = it
                            }
                        )
                        CJButton(
                            text = "create"
                        ) {
                            onIntent(Intent.CreateCollectionRow(rowTextState.text))
                            rowTextState = TextFieldValue("")
                        }
//                        BButton(
//                            text = "generate"
//                        ) {
//                            onIntent(Intent.Generate)
//                        }
                    }
                }
            }
//            Box(
//                modifier = Modifier
//                    .padding(24.dp)
//                    .size(50.dp)
//                    .background(Color.Red, shape = RoundedCornerShape(50.dp))
//                    .clip(RoundedCornerShape(50.dp))
//                    .align(Alignment.BottomEnd)
//                    .clickable {
//                        onIntent(Intent.AddCollectionRow)
//                    }
//            )
//            if (state.fileNode != null) {
//                FileView(
//                    modifier = Modifier,
//                    fileNode = state.fileNode,
//                    onOpenRow = { row ->
//                        onIntent(Intent.OpenCollectionRow(row))
//                    },
//                    onAddRow = { name, rows ->
//                        onIntent(Intent.AddCollectionRow(name, rows))
//                    },
//                    contentFileEdit = { fileType ->
//                        FileEdit(
//                            modifier = Modifier,
//                            text = fileType.value,
//                            onSave = {
//                                onIntent(Intent.ChangeTextNode(fileType, it))
//                            }
//                        )
//                    },
//                    contentCanvas = { fileType ->
//                        Canvas(
//                            startShapes = fileType.shapes,
//                            startConnections = fileType.connections,
//                            onSave = { shapes, connections ->
//                                onIntent(
//                                    Intent.SaveCanvas(
//                                        shapes = shapes,
//                                        connections = connections
//                                    )
//                                )
//                            }
//                        )
//                    }
//                )
//            }
        }
    }
}