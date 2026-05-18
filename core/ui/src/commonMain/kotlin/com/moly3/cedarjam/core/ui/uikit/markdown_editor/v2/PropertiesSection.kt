package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.features.mdprops.DocumentProperty
import com.moly3.cedarjam.core.domain.features.mdprops.PropertyType

/**
 * Intercepts Ctrl/Cmd+Z (undo) and Ctrl/Cmd+Shift+Z / Ctrl+Y (redo) on a
 * property field, so undo/redo works while a property is focused — not only
 * while a body row is focused.
 *
 * Returns true when the event was consumed.
 */
private fun handlePropertyKeyEvent(
    keyEvent: KeyEvent,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false
    val ctrl = keyEvent.isCtrlPressed || keyEvent.isMetaPressed
    if (!ctrl) return false
    return when (keyEvent.key) {
        Key.Z -> {
            if (keyEvent.isShiftPressed) onRedo() else onUndo()
            true
        }
        Key.Y -> {
            onRedo()
            true
        }
        else -> false
    }
}

/**
 * Obsidian-style "Properties" panel — the frontmatter block at the top of a note.
 *
 * Each property is a [name → value] pair with a [com.moly3.cedarjam.core.domain.features.mdprops.PropertyType]. Clicking the type
 * glyph opens a menu to switch the type (Text / Number / Checkbox / Date / List …).
 *
 * @param onPropertiesChange invoked with the new list and a `coalesce` flag.
 *   `coalesce = true` means a per-keystroke value edit that should merge into the
 *   previous undo step; `coalesce = false` means a discrete structural change
 *   (add / remove a property, switch a type, add / remove a list chip).
 * @param onUndo / @param onRedo wired into every property field so Ctrl/Cmd+Z
 *   works while a property — not just a body row — is focused.
 */
@Composable
fun PropertiesSection(
    properties: List<DocumentProperty>,
    onPropertiesChange: (List<DocumentProperty>, coalesce: Boolean) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        properties.forEach { property ->
            PropertyRow(
                property = property,
                onChange = { updated, coalesce ->
                    onPropertiesChange(
                        properties.map { if (it.id == updated.id) updated else it },
                        coalesce,
                    )
                },
                onRemove = {
                    onPropertiesChange(
                        properties.filterNot { it.id == property.id },
                        false,
                    )
                },
                onUndo = onUndo,
                onRedo = onRedo,
            )
        }

        TextButton(
            onClick = { onPropertiesChange(properties + DocumentProperty(), false) },
        ) {
            Text("+ Add property", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PropertyRow(
    property: DocumentProperty,
    onChange: (DocumentProperty, coalesce: Boolean) -> Unit,
    onRemove: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // --- Type selector ----------------------------------------------------
        PropertyTypeSelector(
            selected = property.type,
            onSelect = { newType ->
                // Type switch is structural — discrete undo step.
                onChange(
                    property.copy(type = newType, values = newType.coerceValues(property.values)),
                    false,
                )
            },
        )

        Spacer(Modifier.width(8.dp))

        // --- Property name ----------------------------------------------------
        BasicTextField(
            value = property.name,
            onValueChange = { onChange(property.copy(name = it.replace("\n", "")), true) },
            modifier = Modifier
                .width(120.dp)
                .onPreviewKeyEvent { handlePropertyKeyEvent(it, onUndo, onRedo) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                if (property.name.isEmpty()) {
                    Text(
                        "Property",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                inner()
            },
        )

        Spacer(Modifier.width(12.dp))

        // --- Property value (depends on type) ---------------------------------
        Box(modifier = Modifier.weight(1f)) {
            PropertyValueEditor(
                property = property,
                onChange = onChange,
                onUndo = onUndo,
                onRedo = onRedo,
            )
        }

        // --- Remove -----------------------------------------------------------
        Text(
            text = "✕",
            modifier = Modifier
                .clickable(onClick = onRemove)
                .padding(horizontal = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun PropertyTypeSelector(
    selected: PropertyType,
    onSelect: (PropertyType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .size(26.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(6.dp),
                )
                .clickable { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = selected.glyph,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PropertyType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(type.glyph, modifier = Modifier.width(28.dp))
                            Text(type.label)
                        }
                    },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PropertyValueEditor(
    property: DocumentProperty,
    onChange: (DocumentProperty, coalesce: Boolean) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    when (property.type) {
        PropertyType.Checkbox -> {
            Switch(
                checked = property.singleValue.equals("true", ignoreCase = true),
                // A toggle is one discrete action, not a typing burst.
                onCheckedChange = { onChange(property.withSingleValue(it.toString()), false) },
            )
        }

        PropertyType.List -> {
            // chips of values + an input for a new one
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                property.values.filter { it.isNotBlank() }.forEachIndexed { index, value ->
                    PropertyChip(
                        text = value,
                        onRemove = {
                            onChange(
                                property.copy(values = property.values.filterIndexed { i, _ -> i != index }),
                                false,
                            )
                        },
                    )
                }
                var draft by remember(property.id) { mutableStateOf("") }
                BasicTextField(
                    value = draft,
                    onValueChange = { input ->
                        if (input.endsWith("\n") || input.endsWith(",")) {
                            val token = input.dropLast(1).trim()
                            if (token.isNotEmpty()) {
                                // Committing a chip is a discrete step.
                                onChange(
                                    property.copy(values = property.values.filter { it.isNotBlank() } + token),
                                    false,
                                )
                            }
                            draft = ""
                        } else {
                            draft = input
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .width(90.dp)
                        .onPreviewKeyEvent { handlePropertyKeyEvent(it, onUndo, onRedo) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) {
                            Text(
                                "Add…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                        inner()
                    },
                )
            }
        }

        else -> {
            // Text / Number / Date / DateTime — all single-line text inputs;
            // a real app would swap in a date picker for the Date types.
            BasicTextField(
                value = property.singleValue,
                // Per-keystroke value edit — coalesce into one undo step.
                onValueChange = { onChange(property.withSingleValue(it.replace("\n", "")), true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { handlePropertyKeyEvent(it, onUndo, onRedo) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (property.singleValue.isEmpty()) {
                        Text(
                            property.type.placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun PropertyChip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
        Text(
            "✕",
            modifier = Modifier
                .padding(start = 4.dp)
                .clickable(onClick = onRemove),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

/* ----- helpers ------------------------------------------------------------- */

private fun PropertyType.coerceValues(old: List<String>): List<String> = when (this) {
    PropertyType.List -> old
    else -> listOf(old.firstOrNull().orEmpty())
}

private val PropertyType.glyph: String
    get() = when (this) {
        PropertyType.Text -> "T"
        PropertyType.Number -> "#"
        PropertyType.Checkbox -> "☑"
        PropertyType.Date -> "📅"
        PropertyType.DateTime -> "🕒"
        PropertyType.List -> "≡"
    }

private val PropertyType.placeholder: String
    get() = when (this) {
        PropertyType.Number -> "0"
        PropertyType.Date -> "YYYY-MM-DD"
        PropertyType.DateTime -> "YYYY-MM-DD HH:mm"
        else -> "Empty"
    }