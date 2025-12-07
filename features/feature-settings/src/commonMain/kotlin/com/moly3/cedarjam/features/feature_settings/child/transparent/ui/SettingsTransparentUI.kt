package com.moly3.cedarjam.features.feature_settings.child.transparent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun SettingsTransparentUI() {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
}