package com.moly3.cedarjam.pages.page_collection.ui.internal

import androidx.compose.foundation.layout.Arrangement
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
import com.moly3.cedarjam.core.domain.model.CollectionViewType
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.ButtSnapType
import com.moly3.cedarjam.core.ui.uikit.CJButtSnap
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.core.ui.uikit.JustMenuContent
import com.moly3.cedarjam.pages.page_collection.Intent
import com.moly3.cedarjam.pages.page_collection.State
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.FlowPreview
import vectors.Data

@OptIn(FlowPreview::class)
@Composable
internal fun PageContent(
    state: State,
    onIntent: (Intent) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isIOPressed by remember { mutableStateOf(false) }

    val appTheme = LocalAppTheme.current
    val hazeState = rememberHazeState(blurEnabled = true)
    val hazeStyle = remember(appTheme) {
        HazeStyle(
            backgroundColor = appTheme.colors.backgroundSecondary.copy(alpha = 0.8f),
            tints = listOf(HazeTint(appTheme.colors.backgroundSecondary.copy(0.5f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    Column(modifier = Modifier.wstatusBarsPaddingCJ().navigationBarsPaddingCJ().fillMaxSize().hazeSource(hazeState)) {
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (state.collection != null) {
                when (state.collection.viewType) {
                    CollectionViewType.DataGrid -> {
                        CollectionDataGrid(
                            modifier = Modifier,
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
                            buttType = ButtSnapType.Start
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
                            buttType = ButtSnapType.End
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
            }
        }
    }
    JustMenuContent {
        onIntent(Intent.OpenWorkspaceSettings)
    }
//    FileMenuContent(
//        modifier = Modifier.safeDrawingPadding().fillMaxSize(),
//        borderModifier = Modifier
//            .clip(RoundedCornerShape(16.dp))
//            .hazeEffect(hazeState, hazeStyle)
//        ,
//        annotationsCount = 0,
//        isIOSwitchPressed = isIOPressed,
//        isOpenedMenu = isPressed,
//        openWorkspaceSettings = {
//            onIntent(Intent.OpenWorkspaceSettings)
//        },
//        onIOClick = {
//            isIOPressed = !isIOPressed
//        },
//        onClick = {
//            isPressed = !isPressed
//        }
//    )
}