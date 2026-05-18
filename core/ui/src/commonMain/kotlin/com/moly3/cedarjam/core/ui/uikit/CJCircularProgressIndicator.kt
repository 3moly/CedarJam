package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

@Composable
fun CJCircularProgressIndicator(modifier: Modifier = Modifier, progress: Float? = null) {
    val appTheme = LocalAppTheme.current
    if (progress != null) {
        CircularProgressIndicator(
            modifier = modifier,
            progress = {
                progress
            },
            color = appTheme.primaryColor,
            trackColor = Color.Transparent
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            color = appTheme.primaryColor,
            trackColor = Color.Transparent
        )
    }

}