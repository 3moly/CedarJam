package com.moly3.cedarjam.features.feature_file

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentHistory
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownDecoder
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownEncoder
import com.moly3.cedarjam.core.domain.features.mdprops.MarkdownRow
import com.moly3.cedarjam.core.domain.features.mdprops.RowType
import com.moly3.cedarjam.core.ui.uikit.CJIOSwitch
import com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2.MarkdownEditor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Clock.System

@OptIn(FlowPreview::class)
@Composable
fun FileEdit(
    modifier: Modifier,
    isCompact: Boolean,
    text: String,
    isFocusFirstTime: Boolean = false,
    startFontSize: Float = 16f,
    horizontalPadding: Float = 16f,
    zoom: Float = 1f,
    onSave: (String) -> Unit = {}
) {
    val isRawMode = remember { mutableStateOf(false) }
    var document by remember {
        val document2 = MarkdownDecoder.decode(text)
        mutableStateOf(document2)
    }
    val history = remember { DocumentHistory(document) }
    var doc by remember { mutableStateOf(history.current) }
    var updatedTimingState by remember { mutableStateOf(0L) }
    Column(modifier.fillMaxSize()) {
        CJIOSwitch(modifier = Modifier, height = 24, checked = isRawMode.value, onCheckedChange = {
            isRawMode.value = !isRawMode.value
        })
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (!isRawMode.value) {
                MarkdownEditor(
                    modifier = Modifier.fillMaxSize(),
                    document = doc,
                    onDocumentChange = { updated ->

                        doc = updated
                        updatedTimingState = System.now().toEpochMilliseconds()
                    },
                    history = history,
                )
            } else {
                val rawText = remember {
                    MarkdownEncoder.encode(doc)
                }
                RawEdit(
                    modifier = Modifier,
                    text = rawText,
                    isFocusFirstTime = isFocusFirstTime,
                    startFontSize = startFontSize,
                    horizontalPadding = horizontalPadding,
                    zoom = zoom,
                    onTextEdit = {}
                )
            }
        }
    }


    LaunchedEffect(updatedTimingState) {
        snapshotFlow { updatedTimingState }
            .debounce(1000) // 1 second debounce
            .distinctUntilChanged()
            .collectLatest {
                onSave(MarkdownEncoder.encode(doc))
            }
    }
}