package com.moly3.cedarjam.pages.page_collection.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.pageControlsPadding
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.pages.page_collection.Intent
import com.moly3.cedarjam.pages.page_collection.State
import com.moly3.lazyflow.FlowItemSize
import com.moly3.lazyflow.items
import com.moly3.lazyflow.ui.LazyFlow
import kotlinx.coroutines.FlowPreview
import vector.DotsHorizontal

@OptIn(FlowPreview::class)
@Composable
internal fun PageContent(
    state: State,
    onIntent: (Intent) -> Unit
) {
    Column(
        modifier = Modifier.wstatusBarsPaddingCJ().navigationBarsPaddingCJ().fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CJText(
                    text = state.collection?.name ?: "",
                    fontSize = 21.sp,
                    modifier = Modifier
                )
                CJText(
                    text = state.collection?.viewType?.toString() ?: "",
                    fontSize = 21.sp,
                    modifier = Modifier
                )
            }
            NeumorphicShape(
                modifier = Modifier.size(32.dp),
                painter = rememberVectorPainter(DotsHorizontal)
            ) {
                onIntent(Intent.OpenOptions)
            }
        }
        LazyFlow(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalGap = 4.dp,
            verticalGap = 4.dp
        ) {
            items(items = state.rows, size = { FlowItemSize.Auto }) { item ->
                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .background(
                            LocalAppTheme.current.colors.backgroundPrimary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                }
            }
        }
//        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
//
////            if (state.collection != null) {
////                when (state.collection.viewType) {
////                    CollectionViewType.Word,
////                    CollectionViewType.DataGrid -> {
////                        CollectionDataGrid(
////                            modifier = Modifier,
////                            collection = state.collection,
////                            workspace = state.workspace,
////                            rows = state.rows,
////                            tags = state.tags,
////                            tagCollectionRows = state.tagCollectionRows,
////                            openRow = {
////                                onIntent(
////                                    OpenCollectionRow(
////                                        collectionId = it.collectionId,
////                                        it.id
////                                    )
////                                )
////                            },
////                            renameRow = { row, name ->
////                                onIntent(RenameCollectionRow(row, name))
////                            },
////                            addTag = {
////                                onIntent(AddCollectionRowTag(it))
////                            },
////                            deleteRow = {
////                                onIntent(DeleteCollectionRow(it.id))
////                            },
////                            onSetDocument = { file, row ->
////                                onIntent(SetDocumentToRow(row, file))
////                            },
////                            openDocument = {
////                                onIntent(OpenDocument(it))
////                            }
////                        )
////                    }
////
////                    CollectionViewType.Youtube -> {
////                        CollectionJapan(
////                            rows = state.rows,
////                            isOneWord = false,
////                            isPdfShow = false,
////                            youtubeLink = true,
////                            workspace = state.workspace,
////                            webLink = true,
////                            openWebLink = {
////                                onIntent(OpenWebLink(it))
////                            },
////                            openRow = { row ->
////                                onIntent(
////                                    OpenCollectionRow(
////                                        collectionId = row.collectionId,
////                                        row.id
////                                    )
////                                )
////                            }
////                        )
////                    }
////
////                    CollectionViewType.PDF -> {
////                        CollectionJapan(
////                            rows = state.rows,
////                            isOneWord = false,
////                            isPdfShow = true,
////                            workspace = state.workspace,
////                            youtubeLink = false,
////                            webLink = false,
////                            openRow = { row ->
////                                onIntent(OpenCollectionRow(row.collectionId, row.id))
////                            }
////                        )
////                    }
////
////                    CollectionViewType.Anime -> {
////                        CJText("Anime")
////                    }
////
////                    CollectionViewType.Japan -> {
////                        CollectionJapan(
////                            rows = state.rows,
////                            isOneWord = true,
////                            isPdfShow = false,
////                            youtubeLink = false,
////                            workspace = state.workspace,
////                            webLink = false,
////                            openRow = { row ->
////                                onIntent(OpenCollectionRow(row.collectionId, row.id))
////                            }
////                        )
////                    }
////
////
////                }
////            }
//        }
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
            modifier = Modifier.padding(4.dp).padding(bottom = pageControlsPadding()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var rowTextState by remember { mutableStateOf(TextFieldValue()) }
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                if (state.collection != null) {
//                    Row {
//                        CJButtSnap(
//                            painter = rememberVectorPainter(Data),
//                            isSelected = state.collection.viewType == CollectionViewType.DataGrid,
//                            buttType = ButtSnapType.Start
//                        ) {
//                            onIntent(Intent.ChangeViewType(CollectionViewType.DataGrid))
//                        }
//                        CJButtSnap(
//                            painter = rememberVectorPainter(Data),
//                            isSelected = state.collection.viewType == CollectionViewType.Youtube,
//                            buttType = ButtSnapType.Center
//                        ) {
//                            onIntent(Intent.ChangeViewType(CollectionViewType.Youtube))
//                        }
//                        CJButtSnap(
//                            painter = rememberVectorPainter(Data),
//                            isSelected = state.collection.viewType == CollectionViewType.PDF,
//                            buttType = ButtSnapType.Center
//                        ) {
//                            onIntent(Intent.ChangeViewType(CollectionViewType.PDF))
//                        }
////                                ButtSnap(
////                                    painter = painterResource(MR.images.img_flower),
////                                    isSelected = state.collection.viewType == CollectionViewType.Anime,
////                                    buttType = ButtSnapType.Center
////                                ) {
////                                    onIntent(Intent.ChangeViewType(CollectionViewType.Anime))
////                                }
//                        CJButtSnap(
//                            painter = rememberVectorPainter(Data),
//                            isSelected = state.collection.viewType == CollectionViewType.Japan,
//                            buttType = ButtSnapType.End
//                        ) {
//                            onIntent(Intent.ChangeViewType(CollectionViewType.Japan))
//                        }
//                        CJButtSnap(
//                            painter = rememberVectorPainter(Data),
//                            isSelected = state.collection.viewType == CollectionViewType.Word,
//                            buttType = ButtSnapType.End
//                        ) {
//                            onIntent(Intent.ChangeViewType(CollectionViewType.Word))
//                        }
//                    }
//                }
//
//                CJTextField(
//                    modifier = Modifier.weight(1f),
//                    value = rowTextState,
//                    onValueChanged = {
//                        rowTextState = it
//                    }
//                )
//                CJButton(
//                    text = "create"
//                ) {
//                    onIntent(Intent.CreateCollectionRow(rowTextState.text))
//                    rowTextState = TextFieldValue("")
//                }
//                when (state.collection?.viewType) {
//                    CollectionViewType.Word -> {
//                        CJButton(
//                            text = "import to anki"
//                        ) {
//                            onIntent(Intent.ImportToAnki)
//                        }
//                    }
//
//                    else -> {}
//                }
//            }

        }
    }
}