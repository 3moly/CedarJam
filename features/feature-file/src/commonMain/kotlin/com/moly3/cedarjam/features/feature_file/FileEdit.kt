package com.moly3.cedarjam.features.feature_file

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

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
    onTextEdit: (String) -> Unit = {},
    onSave: (String) -> Unit = {}
) {
    val appTheme = LocalAppTheme.current
    val textEdit = remember {
        mutableStateOf(TextFieldValue(text))
    }
    val focusRequester = remember {
        FocusRequester()
    }
    LaunchedEffect(isFocusFirstTime) {
        if (isFocusFirstTime) {
            focusRequester.requestFocus()
        }
    }
    var linesTextSize by remember(zoom) { mutableStateOf(12f * zoom) }
    var textSize by remember(zoom, startFontSize) { mutableStateOf(startFontSize * zoom) }
    var lineHeight by remember { mutableStateOf(30f) }
    var lineHeightCoef by remember { mutableStateOf(1f) }
    LaunchedEffect(textSize, lineHeightCoef) {
        lineHeight = textSize * lineHeightCoef
    }

    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val lineNumbers = remember {
        mutableStateOf("")
    }
    if (true) {
        LaunchedEffect(Unit) {
            snapshotFlow {
                textEdit.value to textLayoutResult
            }
                .debounce(300L)
                .collectLatest {
                    lineNumbers.value = textLayoutResult?.let { layout ->
                        val text = textEdit.value.text
                        val logicalLines = text.split('\n')
                        val totalVisualLines = layout.lineCount

                        buildString {
                            var logicalLineIndex = 0

                            for (visualLine in 0 until totalVisualLines) {

                                // Check if this visual line starts a new logical line
                                val isLogicalLineStart = if (visualLine == 0) {
                                    true
                                } else {
                                    val prevLineEnd = layout.getLineEnd(visualLine - 1)
                                    prevLineEnd < text.length && text[prevLineEnd] == '\n'
                                }

                                if (isLogicalLineStart && logicalLineIndex < logicalLines.size) {
                                    append(logicalLineIndex + 1)
                                    logicalLineIndex++
                                } else {
                                    // This is a wrapped line, show empty space
                                    append("")
                                }

                                if (visualLine < totalVisualLines - 1) {
                                    append("\n")
                                }
                            }
                        }
                    } ?: run {
                        // Fallback: count logical lines if layout not available yet
                        val logicalLines = textEdit.value.text.count { it == '\n' } + 1
                        buildString {
                            for (i in 1..logicalLines) {
                                append(i)
                                if (i < logicalLines) append("\n")
                            }
                        }
                    }
                }
        }
    }

    val lineNumberTextStyle = LocalTextStyle.current.copy(
        color = Color(0xFF808080),
        fontSize = linesTextSize.sp,
        lineHeight = lineHeight.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
    val editorTextStyle = LocalTextStyle.current.copy(
        color = appTheme.colors.primaryFont,
        fontSize = textSize.sp,
        lineHeight = lineHeight.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            //.background(Color(0xFF1E1E1E))
            .padding(horizontal = (horizontalPadding * zoom).dp)
            .navigationBarsPaddingCJ()
    ) {
        Row(modifier = Modifier.fillMaxHeight().weight(1f)) {
            CJText(
                modifier = Modifier
                    .padding(end = (zoom * 24).dp)
                    .defaultMinSize(minWidth = (30 * zoom).dp)
                    .verticalScroll(scrollState),
                text = lineNumbers.value,
                style = lineNumberTextStyle.copy(textAlign = TextAlign.End)
            )

            BasicTextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .weight(1f)
                    .verticalScroll(scrollState),
                value = textEdit.value,
                textStyle = editorTextStyle,
                onValueChange = {
                    textEdit.value = it
                    onTextEdit(it.text)
                },
                singleLine = false,
                onTextLayout = { layoutResult ->
                    textLayoutResult = layoutResult
                },
                cursorBrush = SolidColor(appTheme.primaryColor),
                decorationBox = {
                    it()
                }
            )
        }
    }
    LaunchedEffect(textEdit.value.text) {
        snapshotFlow { textEdit.value.text }
            .debounce(1000) // 1 second debounce
            .distinctUntilChanged()
            .collectLatest { debouncedText ->
                onSave(debouncedText)
            }
    }
}