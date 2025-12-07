package com.moly3.cedarjam.features.feature_settings.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.flatClickable

@Composable
fun JvmWindowScope.SettingsContent(content: @Composable ColumnScope.() -> Unit) {
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
            content()
        }
    }
}