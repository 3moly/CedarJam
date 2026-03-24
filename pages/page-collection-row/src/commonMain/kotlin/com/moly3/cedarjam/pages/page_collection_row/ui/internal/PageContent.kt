package com.moly3.cedarjam.pages.page_collection_row.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.func.formatEpochMillis
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.CollectionViewType
import com.moly3.cedarjam.core.ui.func.isCompactUI
import com.moly3.cedarjam.core.ui.func.rememberPdfBitmap
import com.moly3.cedarjam.core.ui.func.statusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.AppThemePreview
import com.moly3.cedarjam.pages.page_collection_row.Intent
import com.moly3.cedarjam.pages.page_collection_row.State
import com.moly3.lazyFlow.model.FlowItemSizeMode
import com.moly3.lazyFlow.ui.LazyFlow
import com.moly3.lazyflow.core.model.FlowOrientation

@Composable
internal fun PageContent(state: State, onIntent: (Intent) -> Unit) {
    fun updateRow(row: CollectionRowDTO) {
        onIntent(Intent.Update(row))
    }
    Box(Modifier.wstatusBarsPaddingCJ().fillMaxSize()) {
        if (state.collectionRow != null) {
            val row = state.collectionRow
            val imgBitmap = rememberPdfBitmap(state.collectionRow.fileRelativePath)
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NameField(
                    collectionRow = state.collectionRow,
                    collection = state.collection,
                    scrollState = scrollState,
                    onIntent = onIntent
                )
                val windowWidth = remember { mutableStateOf<Int>(0) }
                LazyFlow(
//                    orientation = orientation,
                    modifier = Modifier.weight(1f).fillMaxWidth().onLayoutRectChanged {
                        windowWidth.value = it.width
                    }, mainAxisArrangement = Arrangement.Center
                ) {
                    if (imgBitmap != null) {
                        item("img") {
                            Image(
                                bitmap = imgBitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .height(200.dp)
                                    .align(Alignment.CenterHorizontally),
                                contentScale = ContentScale.FillHeight
                            )
                        }
                    }
                    item(
                        "fields",
                        sizeMode = FlowItemSizeMode.Exact(
                            DpSize(
                                width = windowWidth.value.dp,
                                height = 400.dp
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            TextPropertyField(
                                placeholder = "mod time",
                                value = state.collectionRow.modifiedTime.formatEpochMillis()
                            )
                            TextPropertyField(
                                placeholder = "web link",
                                value = state.collectionRow.webLink ?: "",
                                onSave = { updateRow(row.copy(webLink = it)) }
                            )
                            TextPropertyField(
                                placeholder = "file path",
                                value = state.collectionRow.fileRelativePath ?: ""
                            )
//                            if (state.collection?.viewType == CollectionViewType.PDF) {
//
//                            }
                            if (state.collection?.viewType == CollectionViewType.Word) {
                                TextPropertyField(
                                    placeholder = "translation",
                                    value = state.collectionRow.translation ?: "",
                                    onSave = { updateRow(row.copy(translation = it)) }
                                )
                                TextPropertyField(
                                    placeholder = "pronunciation",
                                    value = state.collectionRow.pronunciation ?: "",
                                    onSave = { updateRow(row.copy(pronunciation = it)) }
                                )
                                TextPropertyField(
                                    placeholder = "example sentence",
                                    value = state.collectionRow.exampleSentence ?: "",
                                    onSave = { updateRow(row.copy(exampleSentence = it)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PageContentPreview() {
    AppThemePreview(isDark = false) {
        PageContent(
            state = State(
                collection = CollectionDTO(
                    id = 1,
                    name = "japanese words",
                    viewType = CollectionViewType.Word,
                    createdTime = 0,
                    modifiedTime = 0
                ),
                collectionRow = CollectionRowDTO(
                    id = 1L,
                    name = "アニメ",
                    collectionId = 1,
                    fileRelativePath = null,
                    imgRelativePath = null,
                    webLink = null,
                    currentProgress = null,
                    progressMax = null,
                    isCompleted = false,
                    translation = null,
                    pronunciation = null,
                    exampleSentence = null,
                    createdTime = 0L,
                    modifiedTime = 0L,
                    points = 0L,
                )
            ),
            onIntent = {}
        )
    }
}