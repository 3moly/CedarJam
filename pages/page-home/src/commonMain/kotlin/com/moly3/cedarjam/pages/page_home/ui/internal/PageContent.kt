package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.UIStateContentLazy
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.pages.page_home.Intent
import com.moly3.cedarjam.pages.page_home.State
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
internal fun PageContent(
    state: State,
    onIntent: (Intent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item("top"){

            CJSearchTextField(
                modifier = Modifier.fillMaxWidth(),
                isSearchIcon = true,
                placeholderText = "Search...",
                value = state.searchTextFieldValue,
                onValueChange = {
                    onIntent(Intent.SetSearchText(it))
                }
            )
            UIStateContentNoBox(state = state.timeMachinesState) { timeMachines ->
                Row(Modifier) {
                    CJText(text = "Results: ${timeMachines.size}")
                }
            }
        }
        UIStateContentLazy(state = state.timeMachinesState) { timeMachines ->
            for (time in timeMachines) {
                item("machine: ${time}") {
                    when (time) {
                        is TimeMachine.Collection -> {
                            HistoryItem(
                                modifier = Modifier.fillMaxWidth(),
                                time = time.modifiedTime,
                                text = time.collection.name,
                                searchText = state.searchTextFieldValue.text,
                                typeText = "collection",
                                onClick = {
                                    onIntent(Intent.OpenCollection(time.collection.id))
                                }
                            )
                        }

                        is TimeMachine.FileNode -> {
                            HistoryItem(
                                modifier = Modifier.fillMaxWidth(),
                                time = time.modifiedTime,
                                text = time.file.getShortName(),
                                typeText = "file",
                                searchText = state.searchTextFieldValue.text,
                                matches = time.matches,
                                onClick = {
                                    onIntent(Intent.OpenFileNode(time.file.getRelativePath()))
                                }
                            )
                        }

                        is TimeMachine.Tag -> {
                            HistoryItem(
                                modifier = Modifier.fillMaxWidth(),
                                time = time.modifiedTime,
                                text = time.tag.name,
                                searchText = state.searchTextFieldValue.text,
                                typeText = "tag",
                                onClick = {
                                    onIntent(Intent.OpenTag(time.tag.id))
                                }
                            )
                        }

                        is TimeMachine.Row -> {
                            HistoryItem(
                                modifier = Modifier.fillMaxWidth(),
                                time = time.modifiedTime,
                                text = time.row.name,
                                searchText = state.searchTextFieldValue.text,
                                typeText = "row",
                                onClick = {
                                    onIntent(
                                        Intent.OpenRow(
                                            time.row.id,
                                            collectionId = time.row.collectionId
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}