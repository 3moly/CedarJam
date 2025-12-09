package com.moly3.cedarjam.features.feature_settings.child.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJToolbar
import com.moly3.cedarjam.features.feature_settings.child.SettingsContent
import com.moly3.cedarjam.features.feature_settings.child.main.ISettingsMainComponent
import com.moly3.cedarjam.features.feature_settings.child.main.Intent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_title
import com.moly3.cedarjam.ui.f_settings_options
import com.moly3.cedarjam.ui.f_settings_title
import com.moly3.cedarjam.ui.storage
import org.jetbrains.compose.resources.stringResource

@Composable
fun JvmWindowScope.SettingsMainUI(component: ISettingsMainComponent) {
    SettingsContent {
        Box {
            CJDraggableArea {
                CJToolbar(
                    title = stringResource(Res.string.f_settings_title),
                    onClose = { component.onIntent(Intent.Close) }
                )
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            CJText(
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
                text = stringResource(Res.string.f_settings_options),
                fontSize = 12.sp,
                color = LocalAppTheme.current.colors.secondaryFont
            )
            SelectOption(
                isShowBorder = true,
                text = stringResource(Res.string.f_settings_general_title),
                onClick = { component.onIntent(Intent.General) }
            )
            SelectOption(
                isShowBorder = true,
                text = stringResource(Res.string.storage),
                onClick = { component.onIntent(Intent.Storage) }
            )
            SelectOption(
                isShowBorder = true,
                text = "Sync",
                onClick = { component.onIntent(Intent.Sync) }
            )
        }
    }
}

