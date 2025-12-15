package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mikepenz.hypnoticcanvas.shaderBackground
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.shader.NewMagma

@Composable
fun CJTextField(
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    value: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit
) {
    BasicTextField(
        modifier = modifier,
        value = value,
        textStyle = LocalTextStyle.current,
        onValueChange = onValueChanged,
        singleLine = singleLine,
        decorationBox = {
            Box(
                modifier = Modifier.background(LocalAppTheme.current.colors.backgroundPrimary)
                    .padding(4.dp)
            ) {
                it()
            }
        }
    )
}

const val magmaTextFieldMinTextSize = 14f

@Composable
fun CJTextField2(
    modifier: Modifier,
    iconPainter: ImageVector? = null,
    enabled: Boolean = true,
    text: TextFieldState,
    textStyle: TextStyle,
    onDone: () -> Unit,
    onLostFocus: () -> Unit = {}
) {
    val underlineShader = remember { NewMagma() }
    val primaryColor = LocalAppTheme.current.primaryColor
    LaunchedEffect(primaryColor) {
        underlineShader.setActiveColor(primaryColor)
    }
    val focusRequest = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val visibleShader by animateFloatAsState(if (isFocused) 1f else 0f)
    if (isFocused) {
        DisposableEffect(Unit) {
            onDispose {
                onLostFocus()
            }
        }
    }
    val animatedHeightMagmaSize by animateDpAsState((textStyle.fontSize.value * 0.10f).dp)
    CJTextField(
        modifier = modifier
            .focusRequester(focusRequest)
            .onFocusChanged {
                isFocused = it.isFocused
            },
        text = text,
        onDone = onDone,
        textStyle = textStyle,
        enabled = enabled,
        onDecorator = {
            Box(Modifier.width(IntrinsicSize.Min)) {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = isFocused,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        Modifier
                            .alpha(visibleShader)
                            .height(animatedHeightMagmaSize)
                            .fillMaxWidth()
                            .shaderBackground(underlineShader)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (iconPainter != null) {
                        Image(
                            imageVector = iconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
                        )
                    }
                    it()
                }
            }
        }
    )
}

@Composable
fun CJTextField(
    text: TextFieldState,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    onDone: () -> Unit,
    onDecorator: (@Composable (innerTextField: @Composable (() -> Unit)) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        state = text,
        modifier = modifier.onKeyEvent({
            if (it.key == Key.Escape) {
                focusManager.clearFocus()
            } else if (it.key == Key.Enter) {
                onDone()
            }
            true
        }),
        enabled = enabled,
        textStyle = textStyle,
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = keyboardType),
        onKeyboardAction = {
            onDone()
        },
        cursorBrush = SolidColor(LocalAppTheme.current.primaryColor),
        decorator = object : TextFieldDecorator {
            @Composable
            override fun Decoration(innerTextField: @Composable (() -> Unit)) {
                if (onDecorator != null) {
                    onDecorator(innerTextField)
                } else {
                    innerTextField()
                }
            }
        }
    )
}


@Composable
fun CJTextField(
    value: TextFieldValue,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    onValueChange: (TextFieldValue) -> Unit,
    enabled: Boolean = true,
    color: Color = Color.Unspecified,
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    onDone: () -> Unit = {},
    onDecorator: (@Composable (innerTextField: @Composable (() -> Unit)) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onKeyEvent({
            if (it.key == Key.Escape) {
                focusManager.clearFocus()
            } else if (it.key == Key.Enter) {
                onDone()
            }
            true
        }),
        maxLines = 1,
        textStyle = textStyle.merge(color = color),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(LocalAppTheme.current.primaryColor)
    )
}