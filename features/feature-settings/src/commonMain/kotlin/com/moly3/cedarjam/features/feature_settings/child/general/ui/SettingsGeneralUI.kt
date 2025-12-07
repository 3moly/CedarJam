package com.moly3.cedarjam.features.feature_settings.child.general.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.vectors.CloseSM
import com.moly3.cedarjam.core.ui.vectors.Right2
import com.moly3.cedarjam.features.feature_settings.child.general.ISettingsGeneralComponent
import com.moly3.cedarjam.features.feature_settings.child.general.ui.internal.SettingsGeneralUIContent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsGeneralUI(component: ISettingsGeneralComponent) {
    val settings by component.settingsState.collectAsState()
    val scope = rememberCoroutineScope()
    SettingsGeneralUIContent(
        settings = settings,
        onSetSettings = {
            scope.launch {
                component.onSetSettings(it)
            }
        }
    ) {
        component.onClose()
    }
}

@Composable
fun SelectOption(isShowBorder: Boolean, text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .height(44.dp)
            .fillMaxWidth()
            .let {
                if (isShowBorder)
                    it.drawUnder(borderThickness = 0.5.dp)
                else
                    it
            }
            .clickable {
                onClick()
            }
            .padding(horizontal = 24.dp)) {
        CJText(modifier = Modifier.align(Alignment.CenterStart), text = text)
        Image(
            painter = rememberVectorPainter(Right2),
            contentDescription = null,
            modifier = Modifier.size(24.dp).align(Alignment.CenterEnd),
            colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
        )
    }
}

fun Modifier.drawUnder(
    borderColor: Color = Color.Gray,
    borderThickness: Dp = 1.dp
): Modifier {
    return this.drawBehind {
        val stroke = borderThickness.toPx()
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height - stroke / 2f),
            end = Offset(size.width, size.height - stroke / 2f),
            strokeWidth = stroke
        )
    }
}