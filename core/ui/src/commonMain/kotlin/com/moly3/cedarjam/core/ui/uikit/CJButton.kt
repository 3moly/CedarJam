package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle

@Composable
fun CJButton(
    modifier: Modifier = Modifier,
    text: String,
    backColor: Color = LocalAppTheme.current.colors.backgroundSecondary,
    fontColor: Color = LocalAppTheme.current.colors.primaryFont,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1f)

    NeumorphicShape(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .then(
                if (isFocused)
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                else Modifier
            )
            .onPreviewKeyEvent {
                if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                    onClick()
                    true
                } else {
                    false
                }
            }
        ,
        accentColor = if (isFocused) backColor.copy(alpha = 0.8f) else backColor,
        content = {
            CJText(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                text = text,
                style = LocalTextStyle.current,
                fontSize = 12.sp,
                color = fontColor,
                maxLines = 1
            )
        },
        onClick = onClick
    )
}

@Preview
@Composable
private fun PageContentPreview() {
    Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppThemePreview(isDark = false, isFullscreen = false) {
                CJButton(
                    text = "import pdf",
                    onClick = {}
                )
            }
            AppThemePreview(isDark = true, isFullscreen = false) {
                CJButton(
                    text = "export pdf",
                    onClick = {}
                )
            }
        }
    }
}