package com.moly3.cedarjam.features.feature_settings.child.storage.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.func.formatFileSize
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJToolbar
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import com.moly3.cedarjam.features.feature_settings.child.SettingsContent
import com.moly3.cedarjam.features.feature_settings.child.storage.Intent
import com.moly3.cedarjam.features.feature_settings.child.storage.ISettingsStorageComponent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun JvmWindowScope.SettingsStorageUI(component: ISettingsStorageComponent) {
    val state by component.state.collectAsState()
    SettingsContent {
        Box {
            CJDraggableArea {
                CJToolbar(
                    title = stringResource(Res.string.f_settings_general_title),
                    onBack = {
                        component.onIntent(Intent.Back)
                    },
                    onClose = {
                        component.onIntent(Intent.Close)
                    }
                )
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(12.dp).verticalScroll(rememberScrollState())
        ) {
            UIStateContentNoBox(boxModifier = Modifier, state = state.filesState) {
                val allFilesCount = remember(state.allFilesState) {
                    when (val all = state.allFilesState) {
                        is UIState.Error -> 0
                        UIState.Loading -> 0
                        is UIState.Success -> all.data.size
                    }
                }
                val filesSize = remember(it) {
                    formatFileSize(it.sumOf { x -> x.fileSize })
                }
                val groupedFiles = remember(it) {
                    it.groupBy { b ->
                        when (b) {
                            is FileTreeNode.Directory -> null
                            is FileTreeNode.File -> b.name.extension
                        }
                    }.asSequence().sortedByDescending { b -> b.value.size }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CJText(text = "files size: $filesSize")
                    CJText(text = "all files: ${allFilesCount}")

                    ContainerBlock(title = "Database:") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DbItem("Tags", state.tagsCount)
                            DbItem("Collections", state.collectionsCount)
                            DbItem("Rows", state.rowsCount)
                            DbItem("Annotations", state.annotationsCount)
                            DbItem("Tag-Tag", state.tagToTagsCount)
                            DbItem("Tag-File", state.tagToFilesCount)
                            DbItem("Tag-Row", state.tagToRowsCount)
                        }
                    }
                    ContainerBlock(title = "File types:") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (group in groupedFiles) {
                                DbItem(group.key ?: "", group.value.size)
                            }
                        }
                    }

                }
            }
        }

    }
}

@Composable
fun ContainerBlock(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(volumedBorderStroke, shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CJText(text = title)
        content()
    }
}

@Composable
fun DbItem(title: String, count: Int) {
    Row(
        Modifier
            .border(volumedBorderStroke, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CJText(text = title, fontSize = 12.sp, color = LocalAppTheme.current.colors.secondaryFont)
        CJText(text = count.toString(), fontSize = 16.sp)
    }
}