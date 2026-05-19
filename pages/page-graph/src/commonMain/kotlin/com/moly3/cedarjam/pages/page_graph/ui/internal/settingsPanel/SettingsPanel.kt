package com.moly3.cedarjam.pages.page_graph.ui.internal.settingsPanel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.config.GraphPartConfig
import com.moly3.cedarjam.core.domain.model.config.GroupLogic
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJButtonIcon
import com.moly3.cedarjam.core.ui.uikit.CJIOSwitch
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.pages.page_graph.Intent
import com.moly3.cedarjam.pages.page_graph.ui.internal.SettingsSection
import vector.Settings

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
    partConfig: GraphPartConfig,
    onIntent: (Intent) -> Unit
) {
    val filterSearch = remember {
        mutableStateOf(TextFieldValue(partConfig.filter.search))
    }
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
                        CJSearchTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            isSearchIcon = true,
                            placeholderText = "Search...",
                            value = filterSearch.value,
                            onValueChange = {
                                filterSearch.value = it
                                onIntent(Intent.SetFilter(partConfig.filter.copy(search = it.text)))
                            }
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            EnableOption(
                                text = "directories",
                                value = partConfig.filter.isShowDirectories,
                                onClick = {
                                    onIntent(
                                        Intent.SetFilter(
                                            partConfig.filter.copy(
                                                isShowDirectories = !partConfig.filter.isShowDirectories
                                            )
                                        )
                                    )
                                }
                            )
                            EnableOption(
                                text = "tags",
                                value = partConfig.filter.isTags,

                                onClick = {
                                    onIntent(Intent.SetFilter(partConfig.filter.copy(isTags = !partConfig.filter.isTags)))
                                }
                            )
                            EnableOption(
                                text = "orphans",
                                value = partConfig.filter.isOrphans,

                                onClick = {
                                    onIntent(Intent.SetFilter(partConfig.filter.copy(isOrphans = !partConfig.filter.isOrphans)))
                                }
                            )
                            EnableOption(
                                text = "annotations",
                                value = partConfig.filter.isAnnotations,
                                onClick = {
                                    onIntent(Intent.SetFilter(partConfig.filter.copy(isAnnotations = !partConfig.filter.isAnnotations)))
                                }
                            )
                            EnableOption(
                                text = "collections",
                                value = partConfig.filter.isCollections,
                                onClick = {
                                    onIntent(Intent.SetFilter(partConfig.filter.copy(isCollections = !partConfig.filter.isCollections)))
                                }
                            )
                            EnableOption(
                                text = "rows",
                                value = partConfig.filter.isRows,
                                onClick = {
                                    onIntent(Intent.SetFilter(partConfig.filter.copy(isRows = !partConfig.filter.isRows)))
                                }
                            )
                            EnableOption(
                                text = "real files only",
                                value = partConfig.filter.isRealFiles,
                                onClick = {
                                    onIntent(Intent.SetFilter(partConfig.filter.copy(isRealFiles = !partConfig.filter.isRealFiles)))
                                }
                            )
                            EnableOption(
                                text = "gradations",
                                value = partConfig.filter.isGradations,
                                onClick = {
                                    onIntent(Intent.SetFilter(partConfig.filter.copy(isGradations = !partConfig.filter.isGradations)))
                                }
                            )
                        }
                        SettingsSection(
                            title = "Groups"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for ((index, group) in partConfig.groups.withIndex()) {
                                    fun changeGroup(change: (GroupLogic) -> GroupLogic) {
                                        onIntent(
                                            Intent.SetGroups(
                                                partConfig.groups.mapIndexed { i, g ->
                                                    if (i == index) change(g) else g
                                                }
                                            )
                                        )
                                    }

                                    // key the remembered state to index so it doesn't bind to the wrong row
                                    val groupSearch = remember(index) {
                                        mutableStateOf(TextFieldValue(group.name))
                                    }
                                    val filterGroupSearch = remember(index) {
                                        mutableStateOf(TextFieldValue(group.filter))
                                    }
                                    Column(
                                        modifier = Modifier.border(
                                            1.dp,
                                            Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        ).padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {

                                        CJSearchTextField(
                                            modifier = Modifier.fillMaxWidth(),
                                            isSearchIcon = false,
                                            placeholderText = "Group name...",
                                            value = groupSearch.value,
                                            onValueChange = {
                                                groupSearch.value = it
                                                changeGroup {
                                                    group.copy(name = groupSearch.value.text)
                                                }
                                            }
                                        )
                                        CJSearchTextField(
                                            modifier = Modifier.fillMaxWidth(),
                                            isSearchIcon = false,
                                            placeholderText = "Group name...",
                                            value = filterGroupSearch.value,
                                            onValueChange = {
                                                filterGroupSearch.value = it
                                                changeGroup {
                                                    group.copy(filter = filterGroupSearch.value.text)
                                                }
                                            }
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CJText(
                                                text = "Is Land:",
                                                fontSize = 12.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            CJIOSwitch(
                                                modifier = Modifier,
                                                height = 24,
                                                isPressed = group.isLand,
                                                onClick = {
                                                    changeGroup {
                                                        group.copy(isLand = !group.isLand)
                                                    }
                                                })
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(Modifier.weight(1f))
                                            Box(
                                                Modifier.size(24.dp).background(
                                                    group.color,
                                                    shape = RoundedCornerShape(24.dp)
                                                ).flatClickable {
                                                    onIntent(
                                                        Intent.SetGroupColor(
                                                            groupName = group.name,
                                                            color = group.color
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                CJButton(
                                    modifier = Modifier,
                                    text = "Add group"
                                ) {
                                    val groups = partConfig.groups.toMutableList()
                                    val uniqueName = ""
                                    groups.add(
                                        GroupLogic(
                                            isLand = false,
                                            name = "hoho",
                                            filter = "type:tag",
                                            color = Color.Yellow
                                        )
                                    )
                                    onIntent(Intent.SetGroups(groups))
                                }
                            }

//
                        }
                        GraphViewSettingsSection(
                            zoom = zoom,
                            settings = partConfig.config,
                            onIntent = onIntent
                        )
                        GraphTextSettingsSection(settings = partConfig.config, onIntent = onIntent)
                        EnableOption(
                            text = "groups",
                            value = partConfig.config.groupSettings.enabled,
                            onClick = {
                                val groups =
                                    partConfig.config.groupSettings.copy(enabled = !partConfig.config.groupSettings.enabled)
                                onIntent(
                                    Intent.SetGraphSettings(
                                        partConfig.config.copy(
                                            groupSettings = groups
                                        )
                                    )
                                )
                            }
                        )
                        if (partConfig.config.groupSettings.enabled) {
                            GraphGroupSettingsSection(
                                settings = partConfig.config,
                                onIntent = onIntent
                            )
                        }
                    }
                }
            }
        }
    }
}