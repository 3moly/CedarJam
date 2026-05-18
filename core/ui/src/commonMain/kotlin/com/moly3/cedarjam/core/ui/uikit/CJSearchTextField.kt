package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.func.darker
import vector.SearchMagnifyingGlass
import com.moly3.cedarjam.core.ui.volumedBorderStroke

@Composable
fun CJSearchTextField(
    modifier: Modifier = Modifier,
    isSearchIcon: Boolean = false,
    placeholderText: String? = null,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val primaryColor = LocalAppTheme.current.primaryColor
    val primaryFontColor = LocalAppTheme.current.colors.primaryFont
    val textStyle = LocalTextStyle.current
    val isShowPlaceholder = remember(value.text) {
        value.text.isEmpty()
    }
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle.copy(color = primaryFontColor),
        decorationBox = { innerContent ->
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .border(
                        border = volumedBorderStroke,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSearchIcon) {
                    Image(
                        painter = rememberVectorPainter(SearchMagnifyingGlass),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    innerContent()
                    if (isShowPlaceholder && placeholderText != null) {
                        CJText(
                            text = placeholderText,
                            color = primaryFontColor.darker(0.5f)
                        )
                    }
                }
            }
        },
        cursorBrush = SolidColor(primaryColor),
        maxLines = 1
    )
}

@Preview
@Composable
fun SearchTextFieldPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CJSearchTextField(
            modifier = Modifier.fillMaxWidth(),
            value = TextFieldValue(""),
            onValueChange = {})
        CJSearchTextField(
            modifier = Modifier.fillMaxWidth(),
            value = TextFieldValue("smooth"),
            onValueChange = {})
    }
}