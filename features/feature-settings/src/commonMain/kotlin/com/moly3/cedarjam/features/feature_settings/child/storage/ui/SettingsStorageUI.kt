package com.moly3.cedarjam.features.feature_settings.child.storage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.func.formatFileSize
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJToolbar
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import com.moly3.cedarjam.features.feature_settings.child.SettingsContent
import com.moly3.cedarjam.features.feature_settings.child.storage.ISettingsStorageComponent
import com.moly3.cedarjam.features.feature_settings.child.storage.Intent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue

@Composable
fun SettingsStorageUI(component: ISettingsStorageComponent) {
    val state by component.state.collectAsState()
    SettingsContent {
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
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(12.dp).verticalScroll(rememberScrollState())
        ) {
            UIStateContentNoBox(boxModifier = Modifier, state = state.filesState) { data ->
                val allFilesCount = remember(state.allFilesState) {
                    when (val all = state.allFilesState) {
                        is UIState.Error -> 0
                        UIState.Loading -> 0
                        is UIState.Success -> all.data.size
                    }
                }
                val filesSize = remember(data) {
                    formatFileSize(data.sumOf { x -> x.fileSize })
                }
                val groupedFiles = remember(data) {
                    data.groupBy { b ->
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
                    StorageGradationBlock(files = data)

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

/**
 * Aggregated info per file extension used to render the gradation bar
 * and the legend below it.
 */
private data class ExtensionStorageInfo(
    val extension: String,
    val totalSize: Long,
    val filesCount: Int,
    val color: Color,
    val fraction: Float, // 0f..1f portion of the total storage
)

/**
 * Builds a deterministic but visually distinct color for a given extension string.
 * Same extension -> same color across recompositions.
 */
private fun colorForExtension(extension: String): Color {
    // Simple stable hash -> HSV-ish RGB. Avoids extra deps.
    val hash = extension.lowercase().hashCode().absoluteValue
    val hue = (hash % 360)
    // Convert HSV (s=0.55, v=0.85) to RGB manually.
    val s = 0.55f
    val v = 0.85f
    val c = v * s
    val hPrime = hue / 60f
    val x = c * (1f - kotlin.math.abs(hPrime % 2f - 1f))
    val (r1, g1, b1) = when (hPrime.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = v - c
    return Color(r1 + m, g1 + m, b1 + m, 1f)
}

/**
 * Renders a horizontal progress-bar–style gradation that shows which file
 * extensions occupy the most storage. Each segment is proportional to the
 * total size of files with that extension. Below the bar a legend with
 * extension, size, count and percentage is displayed.
 */
@Composable
fun StorageGradationBlock(
    files: List<FileTreeNode>,
) {
    val infos: List<ExtensionStorageInfo> = remember(files) {
        // Flatten: only consider File nodes (directories aggregate their own children
        // and would double-count).
        val onlyFiles = files.filterIsInstance<FileTreeNode.File>()

        val grouped = onlyFiles.groupBy { f ->
            f.name.extension?.lowercase().orEmpty().ifEmpty { "—" }
        }

        val totalSize = onlyFiles.sumOf { it.fileSize }.coerceAtLeast(1L)

        grouped
            .map { (ext, group) ->
                val size = group.sumOf { it.fileSize }
                ExtensionStorageInfo(
                    extension = ext,
                    totalSize = size,
                    filesCount = group.size,
                    color = colorForExtension(ext),
                    fraction = (size.toDouble() / totalSize.toDouble()).toFloat()
                )
            }
            .sortedByDescending { it.totalSize }
    }

    val totalSize = remember(infos) { infos.sumOf { it.totalSize } }
    val biggest = remember(infos) { infos.firstOrNull() }
    val biggestPct = remember(biggest, totalSize) {
        if (biggest == null || totalSize == 0L) 0
        else ((biggest.totalSize.toDouble() / totalSize.toDouble()) * 100.0).toInt()
    }

    ContainerBlock(title = "Storage by extension:") {

        if (infos.isNotEmpty()) {
            GradationBar(infos = infos)
        }

        // Legend.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (info in infos) {
                LegendItem(info = info, totalSize = totalSize)
            }
        }
    }
}

@Composable
private fun GradationBar(infos: List<ExtensionStorageInfo>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Top: labels for segments that are wide enough to fit a label.
        // We use a Row with weights so labels align above their segments.
        Row(Modifier.fillMaxWidth()) {
            for (info in infos) {
                // Skip rendering label for tiny segments to avoid clutter.
                val weight = info.fraction.coerceAtLeast(0.0001f)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (info.fraction >= 0.08f) {
                        CJText(
                            text = ".${info.extension}",
                            fontSize = 10.sp,
                            color = LocalAppTheme.current.colors.secondaryFont,
                        )
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
        ) {
            for ((index, info) in infos.withIndex()) {
                val weight = info.fraction.coerceAtLeast(0.0001f)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(14.dp)
                        .background(info.color)
                )
                // small visual gap between segments, except after the last one
                if (index != infos.lastIndex) {
                    Spacer(Modifier.size(1.dp))
                }
            }
        }
    }
}

@Composable
private fun LegendItem(info: ExtensionStorageInfo, totalSize: Long) {
    val pct = if (totalSize == 0L) 0
    else ((info.totalSize.toDouble() / totalSize.toDouble()) * 100.0).toInt()

    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LocalAppTheme.current.colors.backgroundSecondary)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(info.color)
        )
        CJText(
            text = ".${info.extension}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        CJText(
            text = formatFileSize(info.totalSize),
            fontSize = 12.sp,
            color = LocalAppTheme.current.colors.secondaryFont,
        )
        CJText(
            text = "($pct% • ${info.filesCount})",
            fontSize = 11.sp,
            color = LocalAppTheme.current.colors.secondaryFont,
        )
    }
}