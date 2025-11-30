package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.createHighlightedText

@Composable
fun CJHighlightedText(
    text: String,
    searchText: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    highlightColor: Color = Color.Yellow.copy(alpha = 0.6f),
    highlightTextColor: Color = LocalAppTheme.current.colors.primaryFont
) {
    val highlitedText = remember(text, searchText) {
        if (searchText.isNotEmpty())
            createHighlightedText(
                text = text,
                searchText = searchText,
                highlightColor = highlightColor,
                highlightTextColor = highlightTextColor
            ) else {
            AnnotatedString(text)
        }
    }
    CJText(
        text = highlitedText,
        style = style,
        color = LocalAppTheme.current.colors.primaryFont,
        modifier = modifier,
    )
}