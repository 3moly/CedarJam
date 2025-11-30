package com.moly3.cedarjam.pages.page_tabs

import com.moly3.cedarjam.core.domain.model.PageNameData
import kotlinx.coroutines.flow.Flow

data class TabData(
    val index: Int,
    val nameFlow: Flow<PageNameData?>
)