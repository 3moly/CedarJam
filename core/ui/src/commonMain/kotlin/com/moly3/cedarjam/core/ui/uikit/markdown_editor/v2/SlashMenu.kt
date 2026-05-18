package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.moly3.cedarjam.core.domain.features.mdprops.RowType

/**
 * The "/" menu. Shown anchored under the active row while the user is typing a
 * slash command. Filtering is done by the caller; this is a pure presentational
 * component plus selection callback.
 */
@Composable
fun SlashMenu(
    query: String,
    onSelect: (RowType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    offset: IntOffset = IntOffset.Zero
) {
    val matches = RowType.entries.filter { it.matchesQuery(query) }

    if (matches.isEmpty()) {
        onDismiss()
        return
    }

    Popup(
        offset = offset,
        onDismissRequest = onDismiss,
        alignment = Alignment.BottomStart,
        properties = PopupProperties(focusable = false),
    ) {
        Surface(
            modifier = modifier
                .widthIn(min = 240.dp, max = 320.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(10.dp),
                ),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 280.dp),
                contentPadding = PaddingValues(vertical = 6.dp),
            ) {
                items(matches, key = { it.name }) { type ->
                    SlashMenuItem(type = type, onClick = { onSelect(type) })
                }
            }
        }
    }
}

@Composable
private fun SlashMenuItem(type: RowType, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lightweight glyph stand-in so the module stays icon-library-agnostic.
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.width(28.dp),
        ) {
            Text(
                text = type.glyph,
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = type.menuLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = type.menuHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Fuzzy-ish prefix match against the label, hint, or a short alias. */
internal fun RowType.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim().lowercase()
    return menuLabel.lowercase().contains(q) ||
        menuHint.lowercase().contains(q) ||
        aliases.any { it.startsWith(q) }
}

private val RowType.aliases: List<String>
    get() = when (this) {
        RowType.Paragraph -> listOf("text", "p", "para")
        RowType.Heading1 -> listOf("h1", "heading", "title")
        RowType.Heading2 -> listOf("h2", "heading")
        RowType.Heading3 -> listOf("h3", "heading")
        RowType.BulletList -> listOf("bullet", "ul", "list", "-")
        RowType.NumberedList -> listOf("number", "ol", "list", "1.")
        RowType.Quote -> listOf("quote", "blockquote", ">")
        RowType.Code -> listOf("code", "snippet", "```")
        RowType.Image -> listOf("image", "img", "picture")
        RowType.Divider -> listOf("divider", "hr", "rule", "---")
    }

private val RowType.glyph: String
    get() = when (this) {
        RowType.Paragraph -> "¶"
        RowType.Heading1 -> "H1"
        RowType.Heading2 -> "H2"
        RowType.Heading3 -> "H3"
        RowType.BulletList -> "•"
        RowType.NumberedList -> "1."
        RowType.Quote -> "❝"
        RowType.Code -> "</>"
        RowType.Image -> "▣"
        RowType.Divider -> "—"
    }