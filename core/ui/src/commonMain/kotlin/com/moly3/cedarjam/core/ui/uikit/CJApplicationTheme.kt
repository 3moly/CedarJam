package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.AppSettings
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

@Composable
fun CJApplicationTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {

//    val fontFamily = FontFamily(
//        MR.fonts.geologica_regular.asFont(weight = FontWeight.Normal)!!,
//        MR.fonts.geologica_bold.asFont(weight = FontWeight.Bold)!!,
//        MR.fonts.geologica_semibold.asFont(weight = FontWeight.SemiBold)!!,
//        MR.fonts.geologica_medium.asFont(weight = FontWeight.Medium)!!,
//        MR.fonts.geologica_light.asFont(weight = FontWeight.Light)!!
//    )
    val fontFamily = when (getPlatform()) {
        Platform.Android,
        Platform.Ios,
        is Platform.Jvm -> FontFamily.Default
//            FontFamily(
//            MR.fonts.instrumentsans_regular.asFont(weight = FontWeight.Normal)!!,
//            MR.fonts.instrumentsans_bold.asFont(weight = FontWeight.Bold)!!,
//            MR.fonts.instrumentsans_semibold.asFont(weight = FontWeight.SemiBold)!!,
//            MR.fonts.instrumentsans_medium.asFont(weight = FontWeight.Medium)!!,
//            MR.fonts.geologica_light.asFont(weight = FontWeight.Light)!!
//        )

        Platform.Wasm -> FontFamily.Default
    }

    val textStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color =  appSettings.theme.colors.primaryFont
    )
    val appTheme: CJAppTheme = remember(appSettings.theme) {
        CJAppTheme(
            textStyle = textStyle,
            colors = appSettings.theme.colors,
            primaryColor = appSettings.theme.primaryColor,
            currentTheme = appSettings.theme.colorsType
        )
    }
    val customTextSelectionColors = TextSelectionColors(
        handleColor = appSettings.theme.primaryColor,
        backgroundColor = appSettings.theme.primaryColor.copy(alpha = 0.4f)
    )
    CompositionLocalProvider(
        LocalTextSelectionColors provides customTextSelectionColors,
        LocalAppTheme provides appTheme
    ) {
        content()
    }
}