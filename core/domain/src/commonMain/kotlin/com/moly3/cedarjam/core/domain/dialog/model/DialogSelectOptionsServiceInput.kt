package com.moly3.cedarjam.core.domain.dialog.model

import kotlinx.collections.immutable.ImmutableList

data class DialogSelectOptionsServiceInput(
    val options: ImmutableList<SelectOption>
)

data class SelectOption(
    val text: String,
    val onClick: () -> Unit
)