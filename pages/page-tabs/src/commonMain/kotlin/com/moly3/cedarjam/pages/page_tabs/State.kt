package com.moly3.cedarjam.pages.page_tabs

data class State(
    val currentTabIndex: Int? = null,
    val tabs: List<TabData> = listOf()
)