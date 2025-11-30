package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> CJDataTable(
    isFixedHeader: Boolean = true,
    modifier: Modifier = Modifier,
    itemModifierBuilder: Modifier.(T) -> Modifier = { Modifier },
    headers: List<Header<T>>,
    data: List<T>
) {
    val maxColumnWidthsState = remember(data, headers) { mutableStateMapOf<Int, Float>() }
    Column(modifier = modifier) {
        if (isFixedHeader) {
            DataRow(
                modifier = Modifier.fillMaxWidth(),
                key = data,
                headers = headers,
                maxColumnWidthsState = maxColumnWidthsState
            ) { header ->
                CJText(text = header.headerName)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isFixedHeader) {
                item("header") {
                    DataRow(
                        modifier = Modifier.fillMaxWidth(),
                        key = data,
                        headers = headers,
                        maxColumnWidthsState = maxColumnWidthsState
                    ) { header ->
                        CJText(
                            text = header.headerName
                        )
                    }
                }
            }
            for (item in data) {
                item(key = item.toString()) {
                    DataRow(
                        modifier = Modifier.animateItem().itemModifierBuilder(item),
                        key = data,
                        headers = headers,
                        maxColumnWidthsState = maxColumnWidthsState
                    ) { header ->
                        if (header.content != null) {
                            header.content(item)
                        } else if (header.contentStr != null) {
                            SelectionContainer {
                                CJText(
                                    text = header.contentStr(item)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


data class Header<T>(
    val headerName: String,
    val rowMinWidth: Dp? = null,
    val rowWeight: Float? = null,  // Made nullable
    val rowWidth: Dp? = null,      // Added fixed width option
    val contentStr: ((T) -> String)? = null,
    val content: (@Composable (T) -> Unit)? = null
)

@Composable
private fun <T> DataRow(
    modifier: Modifier,
    key: Any,
    headers: List<Header<T>>,
    maxColumnWidthsState: MutableMap<Int, Float>, // Shared column widths state
    contentItem: @Composable (Header<T>) -> Unit
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val currentWidth = maxWidth
        val maxHeightsState = remember(key, currentWidth) { mutableStateMapOf<Int, Float>() }
        val maxHeight = maxHeightsState.maxOfOrNull { d -> d.value } ?: null
        val maxHeightSave = remember { mutableStateOf<Float?>(null) }

        Row(
            Modifier.fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val height = coordinates.size.height.toFloat()
                    maxHeightSave.value = height
                }) {
            for ((index, header) in headers.withIndex()) {
                // Get the max width for this column
                val maxColumnWidth = maxColumnWidthsState[index]

                DataTableCell(
                    modifier = Modifier
                        .let { baseModifier ->
                            // Apply either weight, fixed width, or max calculated width
                            when {
                                header.rowWeight != null -> baseModifier.weight(header.rowWeight)
                                header.rowWidth != null -> baseModifier.width(header.rowWidth)
                                maxColumnWidth != null -> baseModifier.width(
                                    with(LocalDensity.current) { maxColumnWidth.toDp() }
                                )

                                else -> baseModifier // This will use intrinsic width initially
                            }
                        }
                        .onGloballyPositioned { coordinates ->
                            val height = coordinates.size.height.toFloat()
                            val width = coordinates.size.width.toFloat()

                            maxHeightsState[index] = height

                            // Update max width for this column if no fixed width/weight is specified
                            if (header.rowWeight == null && header.rowWidth == null) {
                                val currentMaxWidth = maxColumnWidthsState[index] ?: 0f
                                if (width > currentMaxWidth) {
                                    maxColumnWidthsState[index] = width
                                }
                            }
                        }
                        .drawBehind {
                            if (maxHeightSave.value != null) {
                                drawRoundRect(
                                    brush = SolidColor(Color.Gray),
                                    size = Size(this.size.width, maxHeightSave.value!!),
                                    style = Stroke(
                                        width = 0.3f
                                    )
                                )
                            }
                        }
                        .let { modifier ->
                            if (header.rowMinWidth != null)
                                modifier.widthIn(min = header.rowMinWidth)
                            else
                                modifier
                        }
                        .let { modifier ->
                            if (maxHeight != null) {
                                modifier.height(with(LocalDensity.current) { maxHeight.toDp() })
                            } else {
                                modifier.height(IntrinsicSize.Min)
                            }
                        }
                ) {
                    contentItem(header)
                }
            }
        }
    }
}

@Composable
private fun DataTableCell(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.padding(8.dp)
    ) {
        content()
    }
}