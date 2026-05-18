package com.moly3.cedarjam.core.domain.model.settings

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.model.AppThemeData
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class AppSettings(
    val theme: AppThemeData
) {
    companion object {
        val defaultSettings = AppSettings(
            theme = AppThemeData.Companion.Default
        )
    }
}