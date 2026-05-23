package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle

@Composable
fun CJTextField(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    value: TextFieldValue,
    maxLines: Int = Int.MAX_VALUE,
    textStyle: TextStyle = LocalTextStyle.current,
    imeAction: ImeAction = ImeAction.Unspecified,
    onAnyAction: KeyboardActionScope.() -> Unit = {},
    onValueChange: (TextFieldValue) -> Unit,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() },
) {
    val focusManager = LocalFocusManager.current
    val appTheme = LocalAppTheme.current

    BasicTextField(
        modifier = modifier.onKeyEvent({
            if (it.key == Key.Escape) {
                focusManager.clearFocus()
                true
            } else if (it.key == Key.Enter) {
                //onDone()
                false
            } else
                false
        }),
        value = value,
        readOnly = readOnly,
        textStyle = textStyle,
        onValueChange = onValueChange,
        singleLine = singleLine,
        maxLines = maxLines,
        cursorBrush = SolidColor(appTheme.primaryColor),
        keyboardActions = KeyboardActions(onAnyAction),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        decorationBox = decorationBox
    )
}

@Composable
fun CJTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    textStyle: TextStyle = LocalTextStyle.current,
    imeAction: ImeAction = ImeAction.Unspecified,
    onAnyAction: KeyboardActionScope.() -> Unit = {},
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() },
) {
    // Hold the TextFieldValue locally so cursor/selection survive recompositions.
    var tfv by remember { mutableStateOf(TextFieldValue(value)) }

    // If the external string changes (undo/redo, programmatic update) and
    // doesn't match our local text, sync it — clamping selection to the new length.
    if (value != tfv.text) {
        tfv = tfv.copy(
            text = value,
            selection = TextRange(value.length.coerceAtMost(tfv.selection.end))
        )
    }

    CJTextField(
        value = tfv,
        onValueChange = { newValue ->
            tfv = newValue
            if (newValue.text != value) {
                onValueChange(newValue.text)
            }
        },
        modifier = modifier,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = textStyle,
        imeAction = imeAction,
        onAnyAction = onAnyAction,
        decorationBox = decorationBox,
    )
}