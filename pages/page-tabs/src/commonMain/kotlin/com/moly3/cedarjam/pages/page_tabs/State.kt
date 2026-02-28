package com.moly3.cedarjam.pages.page_tabs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class State(
    val currentTabIndex: Int? = null,
    val tabs: ImmutableList<TabData> = persistentListOf()
)