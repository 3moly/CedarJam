package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.AppThemeData
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.model.CJText

@Composable
fun CJIOSwitch(
    modifier: Modifier = Modifier,
    height: Int = 90,
    isPressed: Boolean,
    onClick: () -> Unit
) {
    val density = LocalDensity.current

    val transition = updateTransition(
        targetState = isPressed,
        label = "switch_transition"
    )

    val animSpec = tween<Dp>(
        durationMillis = 260,
        easing = FastOutSlowInEasing
    )

    val colorSpec = tween<Color>(
        durationMillis = 260,
        easing = FastOutSlowInEasing
    )

    val animColor by transition.animateColor(
        transitionSpec = { colorSpec },
        label = "color"
    ) { pressed ->
        if (pressed)
            Color(0xFFFF5200)
        else
            LocalAppTheme.current.colors.backgroundPrimary
    }

    val translateI by transition.animateDp(
        transitionSpec = { animSpec },
        label = "translateI"
    ) { pressed ->
        if (pressed) -(height / 2f).dp else -(height * 1.2f).dp
    }

    val translateO by transition.animateDp(
        transitionSpec = { animSpec },
        label = "translateO"
    ) { pressed ->
        if (pressed) (height * 1.2f).dp else (height / 2f).dp
    }

    val padding by transition.animateDp(
        transitionSpec = { animSpec },
        label = "padding"
    ) { pressed ->
        if (pressed) height.dp else (height * 0.1f).dp
    }

    NeumorphicButton(
        modifier = modifier
            .height(height.dp)
            .width((height * 2).dp),
        isPressed = true,
        strength = 1f,
        accentColor = animColor,
        unpressedColor = Color.Red,
        pressedColor = Color.Blue,
        content = {
            CJText(
                modifier = Modifier.graphicsLayer {
                    translationX = with(density) { translateI.toPx() }
                },
                text = "I",
                fontSize = (height / 2f).sp,
                color = Color.White
            )

            CJText(
                modifier = Modifier.graphicsLayer {
                    translationX = with(density) { translateO.toPx() }
                },
                text = "O",
                fontSize = (height / 2f).sp,
                color = Color.White
            )

            NeumorphicButton(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = padding)
                    .size((height * 0.9f).dp),
                isEnabled = false,
                strength = 3f
            )
        }
    ) {
        onClick()
    }
}

@Preview
@Composable
fun CJIOSwitchPreview2() {
    CJApplicationTheme(
        appSettings = AppSettings(AppThemeData.Default.copy(colors = AppColorsData.Light))
    ) {
        var isPressed by remember { mutableStateOf(false) }
        Box(
            Modifier.fillMaxSize().background(Color(0xFF191A1C)),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                CJIOSwitch(
                    height = 30,
                    isPressed = isPressed,
                    onClick = {
                        isPressed = !isPressed
                    })
                CJIOSwitch(isPressed = isPressed, onClick = {
                    isPressed = !isPressed
                })
            }

        }
    }
}