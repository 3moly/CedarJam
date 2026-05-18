package com.moly3.cedarjam.pages.page_tabs

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.ui.model.PageNameData
import kotlinx.coroutines.flow.Flow

@Stable
data class TabData(
    val index: Int,
    val nameFlow: Flow<PageNameData?>
)