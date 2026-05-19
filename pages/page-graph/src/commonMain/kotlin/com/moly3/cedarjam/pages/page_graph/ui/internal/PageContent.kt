package com.moly3.cedarjam.pages.page_graph.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileTypeExt
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData
import com.moly3.cedarjam.core.domain.model.toFileType
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.State
import com.moly3.cedarjam.pages.page_graph.ui.internal.settingsPanel.SettingsPanel
import com.moly3.dataviz.core.graph.engine.IGraphEngine
import com.moly3.dataviz.graph.features.atlas.AtlasTier
import com.moly3.dataviz.graph.ui.AtlasPainterLoader
import com.moly3.dataviz.graph.ui.Graph
import com.moly3.dataviz.graph.ui.TierSelection
import com.moly3.dataviz.graph.ui.rememberAtlasComposer
import com.moly3.dataviz.graph.ui.rememberMovementTracker
import kotlinx.collections.immutable.persistentMapOf
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
@Composable
internal fun PageContent(
    state: State,
    engine: IGraphEngine<String, ObsidianGraphData>,
    onIntent: (Intent) -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(LocalAppTheme.current.colors.backgroundPrimary)
            .onPointerEvent(PointerEventType.Enter) {
                onIntent(Intent.SetIsMouseCaptured(true))
            }
            .onPointerEvent(PointerEventType.Move) {
                onIntent(Intent.SetIsMouseCaptured(true))
            }
            .onPointerEvent(PointerEventType.Exit) {
                onIntent(Intent.SetIsMouseCaptured(false))
            }) {

        val accentColor = LocalAppTheme.current.primaryColor
        val fontColor = LocalAppTheme.current.colors.primaryFont
        val textStyle = LocalTextStyle.current
        val settings = remember(accentColor, state.partConfig.config, fontColor) {

            state.partConfig.config.copy(
                zoom = state.partConfig.config.zoom.copy(stepIn = 1.03f, stepOut = 1f / 1.03f),
                theme = state.partConfig.config.theme.copy(
                    accentColor = accentColor,
                    textColor = fontColor
                ),
            )
        }

        val density = LocalDensity.current

        val context = LocalPlatformContext.current
        val coilImageLoader = remember { ImageLoader(context) }
        val loader: AtlasPainterLoader<String, ObsidianGraphData> = { node ->
            null
//            if (state.config.isShowImages) {
//                when (val dt = node.data) {
//                    is ObsidianGraphData.File -> {
//                        when (dt.extension.toFileType()) {
//                            FileTypeExt.Image -> loadImageSafely(
//                                context,
//                                coilImageLoader,
//                                dt.fullPath
//                            )
//
//                            else -> null
//                        }
//                    }
//
//                    else -> null
//                }
//            } else {
//                null
//            }
        }
        var viewport by remember { mutableStateOf(IntSize.Zero) }
        val movement = rememberMovementTracker(idleMillis = 200)
//        LaunchedEffect(s.velocities) { if (s.velocities.isNotEmpty()) movement.trigger() }
        LaunchedEffect(state.zoom) { movement.trigger() }
        LaunchedEffect(state.graphUserPosition) { movement.trigger() }

        val video = rememberVectorPainter(vector.collection.Youtube)
        val folder = rememberVectorPainter(vector.FolderAdd)
        val tag = rememberVectorPainter(vector.Tag)
        val tiers = remember {
            val isIos = getPlatform() == Platform.Ios
            val atlasses = mutableListOf<AtlasTier>()
            atlasses.add(
                AtlasTier(
                    name = "hq",
                    tileSizePx = if (isIos) 64 else 128,
                    selection = TierSelection.TopByDistance(if (isIos) 10 else 100),
                    isCircular = true,
                    freezeOnMove = true,   // <-- was false
                )
            )
//            if (!isIos) {
//                atlasses.add(
//                    AtlasTier(
//                        name = "lq",
//                        tileSizePx = 32,
//                        selection = TierSelection.All,
//                        isCircular = true,
//                        freezeOnMove = getPlatform() == Platform.Ios
//                    )
//                )
//            }
            atlasses
        }

        //persistentMapOf(
        //                "folder" to folder,
        //                "tag" to tag,
        //                "video" to video
        //            ),
        val handle = rememberAtlasComposer(
            nodes = state.graphNodes,
            tiers = tiers,
            viewport = viewport,
            userPosition = state.graphUserPosition,
            zoom = state.zoom,
            coordinates = state.coordinates,
            loader = if (state.partConfig.filter.isShowImages) loader else suspend { null },
            loaderKey = state.graphNodes.size,
            staticIcons = persistentMapOf(),
            concurrencyLimit = 3,
            staticIconKey = { id, data ->
                when (val dt = data) {
                    is ObsidianGraphData.Annotation -> {
                        null
                    }

                    is ObsidianGraphData.Collection -> null
                    is ObsidianGraphData.CollectionRow -> null
                    is ObsidianGraphData.File -> {
                        if (dt.isDirectory) {
                            "folder"
                        } else {
                            when (dt.extension.toFileType()) {
                                FileTypeExt.Image -> null
                                FileTypeExt.Video -> "video"
                                else -> null
                            }
                        }
                    }

                    is ObsidianGraphData.Tag -> "tag"
                    null -> null
                }
            },
            isMoving = movement.isMoving
        )
        val graphUserPosition = remember { mutableStateOf(state.graphUserPosition) }
        val updatedZoom by rememberUpdatedState(state.zoom)

        Graph(
            userPosition = graphUserPosition.value,
            zoom = state.zoom,
            textStyle = textStyle,
            modifier = Modifier.fillMaxSize().onGloballyPositioned { viewport = it.size },
            engine = engine,
            atlasLayers = handle.atlasLayers,
            getIconKey = handle::resolveIconKey,
            getNodeGroups = { id, data ->
                if (settings.groupSettings.enabled) {
                    state.nodeLands[id] ?: listOf()
                } else
                    listOf()
            },
            getGroupColor = { groupName ->
                state.partConfig.groups.firstOrNull { d -> d.name == groupName }?.color
                    ?: Color.Transparent
            },
            getGroupName = { groupName ->
                groupName
            },
            settings = settings,
            connections = state.connections,
            stateNodes = state.graphNodes,
            coordinates = state.coordinates,
            velocities = mapOf(),
            onWatchPosition = { nodePosition ->
                graphUserPosition.value = nodePosition
                onIntent(Intent.SetGraphUserPosition(nodePosition))
            },
            onPanDelta = { delta ->
                val newValue = graphUserPosition.value + (delta / updatedZoom)
                graphUserPosition.value = newValue
                onIntent(Intent.SetGraphUserPosition(newValue))
            },
            onZoomChange = { isGesture, newValue ->

                onIntent(Intent.SetZoom(isGesture, newValue))
            },
            onNodeClick = { node ->
                val data = node.data
                if (data != null) onIntent(Intent.OpenNodeData(data))
            },
            onCoordinatesUpdate = {
                onIntent(Intent.SetCoordinates(it))
            },
            io = io,
            consume = false,
        )
        SettingsPanel(
            zoom = state.zoom,
            nodesCount = state.graphNodes.size,
            isShowSettings = state.isShowSettings,
            onIntent = onIntent,
            partConfig = state.partConfig
        )
    }
}