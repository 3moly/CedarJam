package com.moly3.cedarjam.features.feature_settings.child.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.drawUnder
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJToolbar
import com.moly3.cedarjam.core.ui.vectors.Right2
import com.moly3.cedarjam.features.feature_settings.child.SettingsContent
import com.moly3.cedarjam.features.feature_settings.child.main.ISettingsMainComponent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_appearance
import com.moly3.cedarjam.ui.f_settings_general_title
import com.moly3.cedarjam.ui.f_settings_options
import com.moly3.cedarjam.ui.f_settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun JvmWindowScope.SettingsMainUI(component: ISettingsMainComponent) {
    SettingsContent {
        Box {
            CJDraggableArea {
                CJToolbar(
                    title = stringResource(Res.string.f_settings_title),
                    onClose = {
                        component.onClose()
                    }
                )
            }
        }

        Column(Modifier.weight(1f).fillMaxWidth()) {
            CJText(
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
                text = stringResource(Res.string.f_settings_options),
                fontSize = 12.sp,
                color = LocalAppTheme.current.colors.secondaryFont
            )
            SelectOption(
                isShowBorder = true,
                text = stringResource(Res.string.f_settings_general_title),
                onClick = {
                    component.openGeneral()
                }
            )
            SelectOption(
                isShowBorder = true,
                text = "Editor",
                onClick = {

                }
            )
            SelectOption(
                isShowBorder = true,
                text = "Toolbar",
                onClick = {

                }
            )
            SelectOption(
                isShowBorder = true,
                text = "Files and links",
                onClick = {

                }
            )
            SelectOption(
                isShowBorder = false,
                text = "Community plugins",
                onClick = {

                }
            )
        }
    }
}

@Composable
fun SelectOption(isShowBorder: Boolean, text: String, onClick: () -> Unit) {
    SelectOption(
        modifier = Modifier.clickable {
            onClick()
        },
        isShowBorder = isShowBorder,
        text = text,
        content = {
            Image(
                painter = rememberVectorPainter(Right2),
                contentDescription = null,
                modifier = Modifier.size(24.dp).align(Alignment.CenterEnd),
                colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
            )
        }
    )
}

@Composable
fun SelectOption(
    modifier: Modifier,
    isShowBorder: Boolean,
    text: String,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .height(44.dp)
            .fillMaxWidth()
            .let {
                if (isShowBorder)
                    it.drawUnder(borderThickness = 0.5.dp)
                else
                    it
            }
//            .clickable {
//                onClick()
//            }
            .padding(horizontal = 24.dp)) {
        CJText(modifier = Modifier.align(Alignment.CenterStart), text = text)
        content()
    }
}