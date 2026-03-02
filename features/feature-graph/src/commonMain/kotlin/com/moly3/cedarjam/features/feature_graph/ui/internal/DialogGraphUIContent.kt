package com.moly3.cedarjam.features.feature_graph.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.func.blendMode
import com.moly3.cedarjam.core.ui.func.getPageTypeIcon
import com.moly3.cedarjam.core.ui.func.rememberCJText
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.ui.uikit.ButtSnapType
import com.moly3.cedarjam.core.ui.uikit.CJButtSnap
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.core.ui.uikit.UIStateSuccessOnly
import com.moly3.cedarjam.features.feature_graph.Intent
import com.moly3.cedarjam.features.feature_graph.State
import com.moly3.cedarjam.features.feature_graph.model.GraphPage
import com.moly3.cedarjam.features.feature_graph.ui.internal.graphPages.GraphPageContent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.tags
import com.moly3.lazyFlow.func.items
import com.moly3.lazyFlow.model.FlowItemSizeMode
import com.moly3.lazyFlow.ui.LazyFlow
import com.moly3.lazyflow.core.model.FlowItemSize
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import org.jetbrains.compose.resources.stringResource
import vectors.BarLeft
import vectors.NetworkNode

@Composable
internal fun DialogGraphUIContent(
    pageNameData: PageNameData?,
    state: State,
    onIntent: (Intent) -> Unit
) {
    val backgroundSecondary = LocalAppTheme.current.colors.backgroundSecondary
    val hazeStyle = remember(backgroundSecondary) {
        HazeStyle(
            backgroundColor = backgroundSecondary,
            tints = listOf(HazeTint(backgroundSecondary.copy(0.2f))),
            blurRadius = 16.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }
    FileMenuContent(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        borderModifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .hazeEffect(LocalHazeState.current, hazeStyle),
        isOpenedMenu = state.isShowContent,
        openWorkspaceSettings = { onIntent(Intent.OpenWorkspaceSettings) },
        onClick = {
            //isPressed = !isPressed
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (pageNameData != null) {
                    val imgVector = pageNameData.pageType.getPageTypeIcon()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (imgVector != null)
                            NeumorphicShape(
                                modifier = Modifier.size(40.dp),
                                isPressed = true,
                                buttonShape = RoundedCornerShape(100.dp),
                                painter = rememberVectorPainter(imgVector),
                                onClick = {}
                            )
                        NeumorphicShape(
                            modifier = Modifier.weight(1f).height(40.dp),
                            isPressed = true,
                            buttonShape = RoundedCornerShape(100.dp),
                            content = {
                                CJText(text = pageNameData.name.rememberCJText())
                            },
                            onClick = {}
                        )
                        NeumorphicShape(
                            modifier = Modifier.size(40.dp),
                            isPressed = true,
                            buttonShape = RoundedCornerShape(100.dp),
                            content = {
                                CJText(text = "pdf")
                            },
                            onClick = {}
                        )
                    }
                }
            }
            Box(Modifier.weight(1f)) {
                when (state.currentPage) {
                    GraphPage.General -> {
                        LazyFlow(
                            Modifier.fillMaxSize(),
                            afterScrollModifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            item(
                                "tags_title",
                                sizeMode = FlowItemSizeMode.FillParent(crossAxisDp = 30.dp)
                            ) {
                                CJText(
                                    text = "# ${stringResource(Res.string.tags)}",
                                    fontSize = 30.sp
                                )
                            }
                            when (val tagsState = state.tagsState) {
                                is UIState.Error,
                                UIState.Loading -> {
                                }

                                is UIState.Success -> {
                                    items(items = tagsState.data, key = { it.id }) { tag ->
                                        NeumorphicShape(
                                            modifier = Modifier.height(30.dp),
                                            accentColor = tag.color,
                                            content = { color ->
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
//                                                    Image(
//                                                        painter = rememberVectorPainter(vectors.Tag),
//                                                        contentDescription = null,
//                                                        colorFilter = ColorFilter.tint(color)
//                                                    )
                                                    CJText(
                                                        modifier = Modifier.blendMode(BlendMode.Difference),
                                                        text = tag.name,
                                                        color = Color.White
                                                    )
                                                }
                                            }) {
                                            onIntent(Intent.OpenNode(ObsidianGraphData.Tag(id = tag.id)))
                                        }
                                    }
                                }
                            }
                            item("add_tag") {
                                NeumorphicShape(
                                    modifier = Modifier.height(30.dp),
                                    content = { color ->
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = rememberVectorPainter(vectors.Add),
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(color)
                                            )
                                            CJText(text = "Add tag", color = color)
                                        }
                                    }) {
                                    onIntent(Intent.AddTag)
                                }
                            }
                        }
                    }

                    GraphPage.Annotations -> {}

                    GraphPage.Graph -> GraphPageContent(
                        state = state,
                        onIntent = onIntent
                    )
                    GraphPage.Files -> {}
                    GraphPage.Rows -> {}
                }
            }
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                CJButtSnap(
                    painter = rememberVectorPainter(BarLeft),
                    buttType = ButtSnapType.Start,
                    isSelected = state.currentPage == GraphPage.General,
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.General))
                }
                CJButtSnap(
                    modifier = Modifier.weight(1f),
                    buttType = ButtSnapType.Center,
                    isSelected = state.currentPage == GraphPage.Files,
                    content = { color ->
                        UIStateSuccessOnly(state = state.filesState) {
                            RowCount(
                                modifier = Modifier,
                                count = it.size,
                                color = color,
                                image = vectors.Note
                            )
                        }
                    }
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.Files))
                }
                CJButtSnap(
                    modifier = Modifier.weight(1f),
                    buttType = ButtSnapType.Center,
                    isSelected = state.currentPage == GraphPage.Rows,
                    content = { color ->
                        UIStateSuccessOnly(state = state.rowsState) {
                            UIStateSuccessOnly(state = state.filesState) {
                                RowCount(
                                    modifier = Modifier,
                                    count = it.size,
                                    color = color,
                                    image = vectors.Data
                                )
                            }
                        }
                    }
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.Rows))
                }
                CJButtSnap(
                    modifier = Modifier.weight(1f),
                    buttType = ButtSnapType.Center,
                    isSelected = state.currentPage == GraphPage.Annotations,
                    content = { color ->
                        UIStateSuccessOnly(state = state.annotationsState) {
                            RowCount(
                                modifier = Modifier,
                                count = it.size,
                                color = color,
                                image = vectors.FileAdd
                            )
                        }
                    }
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.Annotations))
                }
                CJButtSnap(
                    painter = rememberVectorPainter(NetworkNode),
                    buttType = ButtSnapType.End,
                    isSelected = state.currentPage == GraphPage.Graph,
                ) {
                    onIntent(Intent.SetCurrentPage(GraphPage.Graph))
                }
            }
        }
    )
}