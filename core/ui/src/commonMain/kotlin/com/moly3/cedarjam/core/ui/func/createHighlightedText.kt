package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun createHighlightedText(
    text: String,
    searchText: String,
    highlightColor: Color = Color.Yellow,
    highlightTextColor: Color = Color.Black
): AnnotatedString {
    return buildAnnotatedString {
        val searchTextLower = searchText.lowercase()
        val textLower = text.lowercase()
        var lastIndex = 0
        var index = textLower.indexOf(searchTextLower, lastIndex)
        while (index != -1) {
            // Add text before the match
            if (index > lastIndex) {
                append(text.substring(lastIndex, index))
            }
            // Add highlighted match
            withStyle(
                style = SpanStyle(
                    background = highlightColor,
                    color = highlightTextColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, index + searchText.length))
            }

            lastIndex = index + searchText.length
            index = textLower.indexOf(searchTextLower, lastIndex)
        }
        // Add remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}