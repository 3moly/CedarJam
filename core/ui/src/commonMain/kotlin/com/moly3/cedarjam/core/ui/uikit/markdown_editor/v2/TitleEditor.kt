package com.moly3.cedarjam.core.ui.uikit.markdown_editor.v2

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * The document title. Behaves like a single-line heading field.
 * Pressing Enter / Done jumps focus into the first row.
 */
@Composable
fun TitleEditor(
    title: String,
    onTitleChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style: TextStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxWidth()) {
        if (title.isEmpty()) {
            Text(
                text = "Untitled",
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BasicTextField(
            value = title,
            onValueChange = { new ->
                // collapse hard newlines — title is single-line
                onTitleChange(new.replace("\n", ""))
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = style.merge(LocalTextStyle.current),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(
                MaterialTheme.colorScheme.primary,
            ),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onNext = { onSubmit() },
                onDone = { onSubmit() },
            ),
        )
    }
}