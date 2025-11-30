package com.moly3.cedarjam.core.storage.model

import com.moly3.cedarjam.core.domain.func.toColor
import com.moly3.cedarjam.core.domain.func.toHexString
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.ColorsType
import com.moly3.cedarjam.core.domain.model.FontFamilyData
import kotlinx.serialization.Serializable

@Serializable
data class AppThemeJson(
    val primaryColor: String,
    val colorsType: ColorsType,
    val fontFamily: FontFamilyData,
    val colorsData: AppColorsJson
//    val primaryFontColor: String,
//    val font: String,
//    val backgroundPrimary: String,
//    val backgroundSecondary: String,
//    val statusBarBorder: String,
//    val statusBar: String
)

@Serializable
data class AppColorsJson(
    val primaryFont: String,
    val backgroundPrimary: String,
    val backgroundSecondary: String,
    val statusBarBorder: String,
    val statusBar: String,
    val icon: String,
    val divideColor: String,
    val secondaryFont: String,
    val circle: String,
    val circleLine: String
)

fun AppColorsData.toJson(): AppColorsJson {
    return AppColorsJson(
        backgroundPrimary = this.backgroundPrimary.toHexString(),
        backgroundSecondary = this.backgroundSecondary.toHexString(),
        statusBar = this.statusBar.toHexString(),
        statusBarBorder = this.statusBarBorder.toHexString(),
        primaryFont = this.primaryFont.toHexString(),
        icon = this.icon.toHexString(),
        divideColor = this.divide.toHexString(),
        secondaryFont = this.secondaryFont.toHexString(),
        circle = this.circle.toHexString(),
        circleLine = this.circleLine.toHexString()
    )
}

fun AppColorsJson.toData(): AppColorsData {
    return AppColorsData(
        backgroundPrimary = this.backgroundPrimary.toColor(),
        backgroundSecondary = this.backgroundSecondary.toColor(),
        statusBar = this.statusBar.toColor(),
        statusBarBorder = this.statusBarBorder.toColor(),
        primaryFont = this.primaryFont.toColor(),
        icon = this.icon.toColor(),
        divide = this.divideColor.toColor(),
        secondaryFont = this.secondaryFont.toColor(),
        circle = this.circle.toColor(),
        circleLine = this.circleLine.toColor()
    )
}