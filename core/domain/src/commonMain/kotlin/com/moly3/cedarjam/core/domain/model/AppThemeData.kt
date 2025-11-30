package com.moly3.cedarjam.core.domain.model

import androidx.compose.ui.graphics.Color

data class AppThemeData(
    val colorsType: ColorsType,
    val fontFamily: FontFamilyData,
    val primaryColor: Color,
    val colors: AppColorsData
) {
    companion object {
        val Default = AppThemeData(
            colorsType = ColorsType.Unspecified,
            fontFamily = FontFamilyData.Default,
            primaryColor = Color(0xFFCE7B5A),
            colors = AppColorsData.Dark
        )
    }
}

data class AppColorsData(
    val primaryFont: Color,
    val secondaryFont: Color,
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val statusBarBorder: Color,
    val statusBar: Color,
    val icon: Color,
    val divide: Color,
    val circle: Color,
    val circleLine: Color
) {
    companion object {
        val Dark = AppColorsData(
            backgroundPrimary = Color(0xFF1E1E1E),
            backgroundSecondary = Color(0xFF262626),
            statusBarBorder = Color(0xFF363636),
            statusBar = Color(0xFF262626),
            primaryFont = Color.White,
            icon = Color(0xFFBABABA),
            divide = Color.White,
            secondaryFont = Color.Gray,
            circle = Color(0xFFA0A0A8),
            circleLine = Color(0xFF373737)
        )

        val Light = AppColorsData(
            backgroundPrimary = Color(0xFFFFFFFF),
            backgroundSecondary = Color(0xFFF6F6F6),
            statusBarBorder = Color(0xFFF6F6F6),
            statusBar = Color(0xFFE0E0E0),
            primaryFont = Color(0xFF222222),
            icon = Color(0xFF5A5A5A),
            divide = Color.Black,
            secondaryFont = Color.Gray,
            circle = Color(0xFF525252),
            circleLine = Color(0xFFCFCFCF)
        )
    }
}

enum class ColorsType {
    Unspecified,
    Dark,
    Light,
    Custom
}

enum class FontFamilyData {
    Default
}