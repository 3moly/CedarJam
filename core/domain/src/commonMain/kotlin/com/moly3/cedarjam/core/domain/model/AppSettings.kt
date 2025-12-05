package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val currentWorkspaceFullPath: String?,
    val theme: AppThemeData
) {
    companion object {
        val defaultSettings = AppSettings(
            currentWorkspaceFullPath = null,
            theme = AppThemeData.Default
        )
    }
}