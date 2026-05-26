package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.moly3.cedarjam.core.domain.features.mdprops.WikiLinkSyntax

/**
 * Color-only highlight of `[[wiki links]]` for the EDITING field. Identity offset
 * mapping — we don't hide any characters, so the caret math the text field does
 * still lines up with the underlying string.
 */
class WikiLinkVisualTransformation(
    private val linkColor: Color,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (!WikiLinkSyntax.hasAny(text.text)) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val style = SpanStyle(color = linkColor, fontWeight = FontWeight.Medium)
        val styled = buildAnnotatedString {
            append(text)
            WikiLinkSyntax.findAll(text.text).forEach { m ->
                addStyle(style, m.range.first, m.range.last + 1)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}