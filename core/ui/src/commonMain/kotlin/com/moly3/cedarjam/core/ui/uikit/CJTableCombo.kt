package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> CJTableCombo(
    isFixedHeader: Boolean = true,
    modifier: Modifier = Modifier,
    itemModifierBuilder: Modifier.(T) -> Modifier = { Modifier },
    headers: List<Header<T>>,
    data: List<T>
) {
    val rows: List<List<@Composable () -> Unit>> =
        data.map { item ->
            headers.map { header ->
                {
                    Box(modifier.width(header.rowWidth ?: 60.dp)) {
                        if (header.content != null) {
                            header.content(item)
                        } else if (header.contentStr != null) {
                            SelectionContainer {
                                CJText(text = header.contentStr(item))
                            }
                        }
                    }
                }
            }
        }

    CJBasicTable(
        rowCount = data.size,
        columnCount = headers.size,
        cellBorderColor = Color.Gray,
        modifier = modifier,
        cellBorderWidth = 1.dp,
        rows = rows
    )
//    val rows: List<List<@Composable () -> Unit>> = remember(
//        headers,
//        data
//    ) {
//        val mutableRows = mutableListOf<List<@Composable () -> Unit>>()
//
//        for (data in data) {
//            val row = mutableListOf<@Composable () -> Unit>()
//            for(header in headers){
//                row.add {
//                    if (header.content != null) {
//                        header.content(data)
//                    } else if (header.contentStr != null) {
//                        SelectionContainer {
//                            CJText(
//                                text = header.contentStr(data)
//                            )
//                        }
//                    }
////                    header.content?.invoke(data)
////                    header.content(data)
////                    Box(modifier = Modifier.padding(40.dp)) {
////                        CJText(text = "Hello, world!")
////                    }
//                }
//            }
//            mutableRows.add(row)
//        }
//        mutableRows
//    }
//    CJBasicTable(
//        rowCount = data.size,
//        columnCount = headers.size,
//        cellBorderColor = Color.Gray,
//        modifier = modifier,
//        cellBorderWidth = 1.dp,
//        rows = rows
//    )
}