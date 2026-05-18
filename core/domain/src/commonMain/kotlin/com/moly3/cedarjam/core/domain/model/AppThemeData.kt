package com.moly3.cedarjam.core.domain.model

import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.func.ComposeColorSerializer
import kotlinx.serialization.Serializable

@Serializable
data class AppThemeData(
    val colorsType: ColorsType,
    @Serializable(with = ComposeColorSerializer::class)
    val primaryColor: Color,
    val colors: AppColorsData
) {
    companion object {
        val Default = AppThemeData(
            colorsType = ColorsType.Dark,
            primaryColor = Color(0xFFCE7B5A),
            colors = AppColorsData.Dark
        )
    }
}

@Serializable
data class AppColorsData(
    @Serializable(with = ComposeColorSerializer::class)
    val primaryFont: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val secondaryFont: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val backgroundPrimary: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val backgroundSecondary: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val statusBarBorder: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val statusBar: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val icon: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val divide: Color,
    @Serializable(with = ComposeColorSerializer::class)
    val circle: Color,
    @Serializable(with = ComposeColorSerializer::class)
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
    Dark,
    Light
}