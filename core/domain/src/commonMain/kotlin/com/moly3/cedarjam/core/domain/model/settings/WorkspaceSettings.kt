package com.moly3.cedarjam.core.domain.model.settings

import com.moly3.cedarjam.core.domain.model.AppThemeData
import kotlinx.serialization.Serializable

@Serializable
data class WorkspaceSettings(
    val theme: AppThemeData,
    val language: String?
) {
    companion object {
        val defaultSettings = WorkspaceSettings(
            theme = AppThemeData.Default,
            language = "en"
        )
    }
}