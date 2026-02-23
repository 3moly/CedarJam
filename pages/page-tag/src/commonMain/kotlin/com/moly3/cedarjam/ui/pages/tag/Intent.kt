package com.moly3.cedarjam.ui.pages.tag

import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphPresentation

sealed interface Intent {
    data object SetNewTag : Intent
    data object OpenWorkspaceSettings : Intent
    data class SetIsShowGraph(val value: Boolean) : Intent
    data class DeleteLink(val data: ObsidianGraphPresentation) : Intent
    data class OpenLink(val data: ObsidianGraphPresentation) : Intent
}