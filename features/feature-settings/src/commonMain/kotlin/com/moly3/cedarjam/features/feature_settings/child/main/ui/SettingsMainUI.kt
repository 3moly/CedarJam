package com.moly3.cedarjam.features.feature_settings.child.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.features.feature_settings.child.main.ISettingsMainComponent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsMainUI(component: ISettingsMainComponent) {
    val appTheme = LocalAppTheme.current
    Box(
        Modifier
            .fillMaxSize()
            .background(appTheme.colors.backgroundPrimary)
            .flatClickable {}
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column {
            Box(Modifier.height(46.dp).fillMaxWidth().border(0.5.dp, Color.Gray)) {
                CJText(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.f_settings_title)
                )
            }
            Column(Modifier.weight(1f).fillMaxWidth()) {

            }
        }
    }
}