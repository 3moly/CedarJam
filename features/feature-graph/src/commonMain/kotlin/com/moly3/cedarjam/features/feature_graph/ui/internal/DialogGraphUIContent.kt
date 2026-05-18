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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import coil3.compose.AsyncImage
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalHazeState
import com.moly3.cedarjam.core.ui.compositions.LocalWorkspacePath
import com.moly3.cedarjam.core.ui.func.blendMode
import com.moly3.cedarjam.core.ui.func.getPageTypeIcon
import com.moly3.cedarjam.core.ui.func.rememberCJText
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
import com.moly3.cedarjam.core.ui.uikit.TimeMachineList
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.tags
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import com.moly3.dataviz.core.graph.engine.impl.ultra.UltraFastEngine
import com.moly3.lazyflow.FlowItemSize
import com.moly3.lazyflow.items
import com.moly3.lazyflow.ui.LazyFlow
import com.moly3.lazyflow.ui.rememberLazyFlowState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import vector.BarLeft
import vector.NetworkNode
import vector.collection.Note

@Composable
internal fun DialogGraphUIContent(
     engine: IGraphEngine<String, ObsidianGraphData>,
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
                            modifier = Modifier.fillMaxSize(),
//                            afterScrollModifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            item(
                                "tags_title",
                                size = FlowItemSize.FillCrossAxis(crossAxis = 30.dp)
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
                                            content = { _ ->
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
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
                                                painter = rememberVectorPainter(vector.Add),
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

                    GraphPage.Annotations -> {
                        UIStateContentNoBox(state = state.annotationsState) {
                            val annotations = remember(it) {
                                it.sortedBy { d -> d.dataPoint }.toPersistentList()
                            }
                            val annotationsScrollState =
                                rememberLazyFlowState(rememberScrollState(initial = state.annotationsScrollState))
                            LaunchedEffect(annotationsScrollState.scrollState.value) {
                                onIntent(Intent.AnnotationsScrollState(annotationsScrollState.scrollState.value))
                            }
                            val workspaceFullPath = LocalWorkspacePath.current
                            LazyFlow(
                                state = annotationsScrollState,
                                modifier = Modifier.fillMaxSize(),
                                verticalGap = 8.dp
                            ) {
                                for (item in annotations) {
                                    item(
                                        key = item.id,
                                        size = FlowItemSize.FillCrossAxis(crossAxis = 100.dp)
                                    ) {
                                        val ful = remember(workspaceFullPath, item.id) {
                                            pathWrapper(
                                                workspaceFullPath,
                                                hiddenDirectory,
                                                "image_cache",
                                                "annotation_${item.id}.png"
                                            ).pathString
                                        }
                                        NeumorphicShape(
                                            modifier = Modifier.fillMaxSize(),
                                            buttonShape = RoundedCornerShape(16.dp),
                                            content = {
                                                Row(
                                                    Modifier.padding(horizontal = 16.dp)
                                                        .fillMaxSize(),
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AsyncImage(
                                                        modifier = Modifier.width(75.dp),
                                                        model = ful,
                                                        contentDescription = null
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        CJText(
                                                            text = item.dataPath,
                                                            modifier = Modifier
                                                        )
                                                        CJText(
                                                            text = item.dataPoint.toString(),
                                                            modifier = Modifier
                                                        )
                                                    }
                                                    NeumorphicShape(
                                                        modifier = Modifier.size(40.dp),
                                                        painter = rememberVectorPainter(vector.TrashEmpty),
                                                    ) {
                                                        onIntent(Intent.RemoveAnnotation(item.id))
                                                    }
                                                }
                                            }
                                        ) {
                                            onIntent(Intent.OpenPdfPage(item.dataPoint.toInt()))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    GraphPage.Graph -> GraphPageContent(
                        state = state,
                        onIntent = onIntent,
                        engine = engine
                    )

                    GraphPage.Files -> {
                        val timeMachines = remember(state.filesState) {
                            when (state.filesState) {
                                is UIState.Error -> persistentListOf()
                                UIState.Loading -> persistentListOf()
                                is UIState.Success -> {
                                    state.filesState.data.mapNotNull {
                                        when (it) {
                                            is FileTreeNode.Directory -> null
                                            is FileTreeNode.File -> TimeMachine.FileNode(
                                                it,
                                                null,
                                                modifiedTime = it.modifiedTime
                                            )
                                        }
                                    }.toPersistentList()
                                }
                            }
                        }

                        TimeMachineList(
                            list = timeMachines,
                            workspaceFullPath = LocalWorkspacePath.current,
                            scrollState = rememberLazyFlowState(),
                            onClick = { onIntent(Intent.OpenTimeMachine(it)) }
                        )
                    }

                    GraphPage.Rows -> {
                        val timeMachines = remember(state.rowsState) {
                            when (state.rowsState) {
                                is UIState.Error -> persistentListOf()
                                UIState.Loading -> persistentListOf()
                                is UIState.Success -> {
                                    state.rowsState.data.map {
                                        TimeMachine.Row(
                                            it,
                                            modifiedTime = it.modifiedTime
                                        )
                                    }.toPersistentList()
                                }
                            }
                        }

                        TimeMachineList(
                            list = timeMachines,
                            workspaceFullPath = LocalWorkspacePath.current,
                            scrollState = rememberLazyFlowState(),
                            onClick = { onIntent(Intent.OpenTimeMachine(it)) }
                        )
                    }
                }
            }
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                CJButtSnap(
                    painter = rememberVectorPainter(BarLeft),
                    buttType = ButtSnapType.Start,
                    isSelected = state.currentPage == GraphPage.General,
                ) {
                    onIntent(Intent.SetCurrentTabPage(GraphPage.General))
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
                                image = Note
                            )
                        }
                    }
                ) {
                    onIntent(Intent.SetCurrentTabPage(GraphPage.Files))
                }
                CJButtSnap(
                    modifier = Modifier.weight(1f),
                    buttType = ButtSnapType.Center,
                    isSelected = state.currentPage == GraphPage.Rows,
                    content = { color ->
                        UIStateSuccessOnly(state = state.rowsState) {
                            UIStateSuccessOnly(state = state.rowsState) {
                                RowCount(
                                    modifier = Modifier,
                                    count = it.size,
                                    color = color,
                                    image = vector.Data
                                )
                            }
                        }
                    }
                ) {
                    onIntent(Intent.SetCurrentTabPage(GraphPage.Rows))
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
                                image = vector.FileAdd
                            )
                        }
                    }
                ) {
                    onIntent(Intent.SetCurrentTabPage(GraphPage.Annotations))
                }
                CJButtSnap(
                    painter = rememberVectorPainter(NetworkNode),
                    buttType = ButtSnapType.End,
                    isSelected = state.currentPage == GraphPage.Graph,
                ) {
                    onIntent(Intent.SetCurrentTabPage(GraphPage.Graph))
                }
            }
        }
    )
}