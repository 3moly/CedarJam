package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.ColorsType

data class CJAppTheme(
    val currentTheme: ColorsType,
    val textStyle: TextStyle,
    val primaryColor: Color,
    val colors: AppColorsData
) {
    companion object Companion {
        val Default = CJAppTheme(
            textStyle = TextStyle.Companion.Default,
            colors = AppColorsData.Dark,
            primaryColor = Color.Red,
            currentTheme = ColorsType.Unspecified
        )
    }
}