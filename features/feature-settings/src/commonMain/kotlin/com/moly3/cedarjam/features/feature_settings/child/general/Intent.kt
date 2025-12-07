package com.moly3.cedarjam.features.feature_settings.child.general

import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings

sealed interface Intent {
    data object Back : Intent
    data object Close : Intent
    data object UploadFont : Intent
    data class SetSettings(val data: WorkspaceSettings) : Intent
}