package com.moly3.cedarjam.ui.pages.tag

import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphPresentation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class State(
    val tagState: UIState<TagDTO, Unit> = UIState.Loading,
    val connections: ImmutableList<ObsidianGraphPresentation> = persistentListOf()
)

