package com.moly3.cedarjam.pages.page_tabs

import com.moly3.cedarjam.core.domain.model.PageNameData

sealed class Intent {
    data object AddNewTabs : Intent()
    data object AddNewTab : Intent()
    data class BringToFrontTab(val index: Int) : Intent()
    data class CloseTab(val indexToDelete: Int) : Intent()
    data class OnFileReveal(val pageType: PageNameData.PageType) : Intent()
}