package com.moly3.cedarjam.pages.page_graph.ui.internal.settingsPanel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.func.ignoreIndexSqlDatabaseName
import com.moly3.cedarjam.core.domain.model.node.GraphSettingsConfig
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.ButtSnapType
import com.moly3.cedarjam.core.ui.uikit.CJButtSnap
import com.moly3.cedarjam.core.ui.uikit.CJButtonIcon
import com.moly3.cedarjam.core.ui.uikit.CJIOSwitch
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.dataviz.core.graph.model.GraphSettings
import vector.Settings
import vector.Tag
import vector.collection.File05
import vector.collection.FileAttach01
import vector.collection.Note

@Composable
fun EnableOption(text: String, value: Boolean, onClick: () -> Unit) {
    NeumorphicShape(
        isPressed = value,
        onClick = onClick,
        pressedIconColor = LocalAppTheme.current.colors.primaryFont,
        unpressedIconColor = LocalAppTheme.current.colors.secondaryFont,
        content = { color ->
            Box(
                Modifier.padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CJText(modifier = Modifier, text = text, color = color)
            }
        }
    )
}

@Composable
fun BoxScope.SettingsPanel(
    zoom: Float,
    isShowSettings: Boolean,
    nodesCount: Int,
    config: GraphSettingsConfig,
    settings: GraphSettings,
    onIntent: (Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .wstatusBarsPaddingCJ()
            .navigationBarsPaddingCJ()
            .padding(16.dp)
            .align(Alignment.TopEnd)
            .clip(RoundedCornerShape(8.dp))
            .background(LocalAppTheme.current.colors.backgroundSecondary)
            .verticalScroll(rememberScrollState())
    ) {
        val settingsWidth by animateDpAsState(
            targetValue = if (isShowSettings) 250.dp else 48.dp,
            label = "settingsWidth"
        )
        Column(
            modifier = Modifier.width(settingsWidth).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (isShowSettings) {
                    CJText(text = "Nodes: ${nodesCount}", modifier = Modifier.weight(1f))
                }
                CJButtonIcon(imageVector = Settings, onClick = {
                    onIntent(Intent.SetIsShowSettings(!isShowSettings))
                })
            }

            AnimatedVisibility(
                visible = isShowSettings,
                // 2. Explicitly define the enter/exit transitions to fade and scale vertically
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column {
                    Column(
                        modifier = Modifier
                            .requiredWidth(234.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            EnableOption(
                                text = "directories",
                                value = config.isShowDirectories,
                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isShowDirectories = !config.isShowDirectories)))
                                }
                            )
                            EnableOption(
                                text = "tags",
                                value = config.isTags,

                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isTags = !config.isTags)))
                                }
                            )
                            EnableOption(
                                text = "orphans",
                                value = config.isOrphans,

                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isOrphans = !config.isOrphans)))
                                }
                            )
                            EnableOption(
                                text = "annotations",
                                value = config.isAnnotations,
                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isAnnotations = !config.isAnnotations)))
                                }
                            )
                            EnableOption(
                                text = "collections",
                                value = config.isCollections,
                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isCollections = !config.isCollections)))
                                }
                            )
                            EnableOption(
                                text = "rows",
                                value = config.isRows,
                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isRows = !config.isRows)))
                                }
                            )
                            EnableOption(
                                text = "real files only",
                                value = config.isRealFiles,
                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isRealFiles = !config.isRealFiles)))
                                }
                            )
                            EnableOption(
                                text = "gradations",
                                value = config.isGradations,
                                onClick = {
                                    onIntent(Intent.SetConfig(config.copy(isGradations = !config.isGradations)))
                                }
                            )
                        }
                        GraphViewSettingsSection(
                            zoom = zoom,
                            settings = settings,
                            onIntent = onIntent
                        )
                        GraphTextSettingsSection(settings = settings, onIntent = onIntent)
                        EnableOption(
                            text = "groups",
                            value = settings.groupSettings.enabled,
                            onClick = {
                                val groups =
                                    settings.groupSettings.copy(enabled = !settings.groupSettings.enabled)
                                onIntent(Intent.SetGraphSettings(settings.copy(groupSettings = groups)))
                            }
                        )
                        if (settings.groupSettings.enabled) {
                            GraphGroupSettingsSection(settings = settings, onIntent = onIntent)
                        }
                    }
                }
            }
        }
    }
}