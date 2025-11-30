package com.moly3.cedarjam.core.storage.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsJson(
    val currentWorkspaceFullPath: String?,
    val appTheme: AppThemeJson
)