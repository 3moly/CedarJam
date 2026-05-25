package com.moly3.cedarjam.core.domain.dialog

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.model.config.GraphPartConfig
import com.moly3.cedarjam.core.domain.model.config.GraphSaveConfig
import com.moly3.cedarjam.core.domain.service.WorkspaceSession

@Stable
class DialogGraphConfigsService(
    register: IDialogRegister,
    val workspaceSession: WorkspaceSession,
    val dialogDeleteService: DialogDeleteService
) : GlobalDialog<DialogGraphConfigsService.Input, GraphSaveConfig?>(
    closeValue = null, register = register
) {
    @Stable
    data class Input(val part: GraphPartConfig)
}