package com.moly3.cedarjam.core.domain.model.settings

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.model.AppThemeData
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class WorkspaceSettings(
    val theme: AppThemeData,
    val density: Float,
    val fontScale: Float,
    val language: String?
) {
    companion object {
        val defaultSettings = WorkspaceSettings(
            theme = AppThemeData.Default,
            language = "en",
            density = 1f,
            fontScale = 1f
        )
    }
}