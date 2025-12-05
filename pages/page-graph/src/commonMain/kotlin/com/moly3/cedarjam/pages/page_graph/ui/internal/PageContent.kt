package com.moly3.cedarjam.pages.page_graph.ui.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.State
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.onPointerEvent
import com.moly3.cedarjam.core.ui.uikit.CJButtonIcon
import com.moly3.cedarjam.core.ui.uikit.CJSlider
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.vectors.SettingsFuture
import com.moly3.dataviz.graph.ui.Graph
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
internal fun PageContent(state: State, onIntent: (Intent) -> Unit) {
    val settingsWidth by animateDpAsState(if (state.isShowSettings) 130.dp else 48.dp)
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
        Graph(
            connections = state.connections,
            stateNodes = state.graphNodes,
            viewSettings = state.graphViewSettings,
            coordinates = state.coordinates,
            velocities = state.velocities,
            zoom = state.zoom,
            onZoomChange = {
                onIntent(Intent.SetZoom(it))
            },
            userPosition = state.graphUserPosition,
            onCentralGlobalPosition = {
                onIntent(Intent.SetGraphUserPosition(it))
            },
            onNodeClick = { node ->
                val data = node.data
                if (data != null) {
                    onIntent(Intent.OpenNodeData(data))
                }
            },
            onCoordinatesUpdate = {
                onIntent(Intent.SetCoordinates(it))
            },
            onVelocitiesUpdate = {
                onIntent(Intent.SetVelocities(it))
            },
            io = io,
            fontColor = LocalAppTheme.current.colors.primaryFont,
            circleColor = Color.Yellow,
            primaryColor = Color.Blue,
            circleLineColor = LocalAppTheme.current.colors.divide,
            consume = false,
            textStyle = LocalTextStyle.current
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(8.dp))
                .background(LocalAppTheme.current.colors.backgroundSecondary)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.width(settingsWidth).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                CJButtonIcon(imageVector = SettingsFuture, onClick = {
                    onIntent(Intent.SetIsShowSettings(!state.isShowSettings))
                })
                AnimatedVisibility(state.isShowSettings) {
                    Column {
                        SwitchOption(
                            modifier = Modifier,
                            text = "Collections",
                            value = state.config.isCollections,
                            onClick = {
                                onIntent(Intent.SetConfig(state.config.copy(isCollections = !state.config.isCollections)))
                            }
                        )
                        SwitchOption(
                            modifier = Modifier,
                            text = "Rows",
                            value = state.config.isRows,
                            onClick = {
                                onIntent(Intent.SetConfig(state.config.copy(isRows = !state.config.isRows)))
                            }
                        )
                        SwitchOption(
                            modifier = Modifier,
                            text = "Tags",
                            value = state.config.isTags,
                            onClick = {
                                onIntent(Intent.SetConfig(state.config.copy(isTags = !state.config.isTags)))
                            }
                        )
                        SwitchOption(
                            modifier = Modifier,
                            text = "Directories",
                            value = state.config.isShowDirectories,
                            onClick = {
                                onIntent(Intent.SetConfig(state.config.copy(isShowDirectories = !state.config.isShowDirectories)))
                            }
                        )
                        SwitchOption(
                            modifier = Modifier,
                            text = "Orphans",
                            value = state.config.isOrphans,
                            onClick = {
                                onIntent(Intent.SetConfig(state.config.copy(isOrphans = !state.config.isOrphans)))
                            }
                        )
                        SwitchOption(
                            modifier = Modifier,
                            text = "Real files only",
                            value = state.config.isRealFiles,
                            onClick = {
                                onIntent(Intent.SetConfig(state.config.copy(isRealFiles = !state.config.isRealFiles)))
                            }
                        )
                        SwitchOption(
                            modifier = Modifier,
                            text = "Gradation",
                            value = state.config.isGradations,
                            onClick = {
                                onIntent(Intent.SetConfig(state.config.copy(isGradations = !state.config.isGradations)))
                            }
                        )
                        CJText("zoom: ${state.zoom}")
                        CJText(
                            "center force: ${state.graphViewSettings.centerForce}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.centerForce,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            centerForce = it
                                        )
                                    )
                                )
                            },
                            valueRange = 0.001f..1f
                        )
                        CJText(
                            "link force: ${state.graphViewSettings.linkForce}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.linkForce,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            linkForce = it
                                        )
                                    )
                                )
                            },
                            valueRange = 0.00001f..10f
                        )
                        CJText(
                            "link distance: ${state.graphViewSettings.linkDistance}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.linkDistance,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            linkDistance = it
                                        )
                                    )
                                )
                            },
                            valueRange = 1f..500f
                        )
                        CJText(
                            "circle size: ${state.graphViewSettings.circleSize}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.circleSize,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            circleSize = it
                                        )
                                    )
                                )
                            },
                            valueRange = 0.1f..50f
                        )
                        CJText(
                            "repel force: ${state.graphViewSettings.repelForce}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.repelForce,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            repelForce = it
                                        )
                                    )
                                )
                            },
                            valueRange = 0.1f..100000f
                        )
                        CJText(
                            "unconnectedRepulsionMultiplier: ${state.graphViewSettings.unconnectedRepulsionMultiplier}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.unconnectedRepulsionMultiplier,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            unconnectedRepulsionMultiplier = it
                                        )
                                    )
                                )
                            },
                            valueRange = 1f..10f
                        )
                        CJText(
                            "minMutualConnectionsForClustering: ${state.graphViewSettings.minMutualConnectionsForClustering}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.minMutualConnectionsForClustering.toFloat(),
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            minMutualConnectionsForClustering = it.toInt()
                                        )
                                    )
                                )
                            },
                            valueRange = 1f..10f
                        )
                        CJText(
                            "clusteringForce: ${state.graphViewSettings.clusteringForce}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.clusteringForce,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            clusteringForce = it
                                        )
                                    )
                                )
                            },
                            valueRange = 1f..50f
                        )
                        CJText(
                            "longDistanceLinkMultiplier: ${state.graphViewSettings.longDistanceLinkMultiplier}"
                        )
                        CJSlider(
                            modifier = Modifier.width(150.dp),
                            value = state.graphViewSettings.longDistanceLinkMultiplier,
                            onValueChange = {
                                onIntent(
                                    Intent.SetGraphViewSettings(
                                        state.graphViewSettings.copy(
                                            longDistanceLinkMultiplier = it
                                        )
                                    )
                                )
                            },
                            valueRange = 1f..1000f
                        )
                    }
                }
            }
        }
    }
}