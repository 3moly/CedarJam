package com.moly3.cedarjam.pages.page_collection_row.ui.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField

@Composable
fun TextPropertyField(
    placeholder: String,
    value: String,
    onSave: ((String) -> Unit)? = null
) {
    var textState by remember {
        mutableStateOf(TextFieldValue(value))
    }
    LaunchedEffect(value) {
        if (textState.text != value) {
            textState = textState.copy(text = value)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CJText(
            modifier = Modifier.width(120.dp),
            text = placeholder,
            fontSize = 12.sp,
            color = LocalAppTheme.current.colors.secondaryFont
        )
        CJTextField(
            modifier = Modifier.weight(1f),
            readOnly = onSave == null,
            value = textState,
            imeAction = ImeAction.Done,
            onAnyAction = {
                onSave?.invoke(textState.text)
            },
            onValueChange = {
                textState = it
            }
        )
    }
}