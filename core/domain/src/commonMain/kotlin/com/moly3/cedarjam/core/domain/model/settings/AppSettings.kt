package com.moly3.cedarjam.core.domain.model.settings

import com.moly3.cedarjam.core.domain.model.AppThemeData
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val theme: AppThemeData
) {
    companion object {
        val defaultSettings = AppSettings(
            theme = AppThemeData.Companion.Default
        )
    }
}