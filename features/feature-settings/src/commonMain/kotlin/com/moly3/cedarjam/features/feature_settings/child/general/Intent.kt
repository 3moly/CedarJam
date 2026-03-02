package com.moly3.cedarjam.features.feature_settings.child.general

import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.ColorsType

sealed interface Intent {
    data object Back : Intent
    data class ChangePrimaryColor(val color: Color) : Intent
    data object Close : Intent
    data object UploadFont : Intent
    data class SetLanguage(val code: String) : Intent
    data class SetDensity(val density: Float, val fontScale: Float) : Intent
    data class SetTheme(val colorsType: ColorsType, val colors: AppColorsData) : Intent
}