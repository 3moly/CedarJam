package com.moly3.cedarjam.pages.page_tab

import com.moly3.cedarjam.core.ui.model.PageNameData

sealed class Intent {
    data object Back : Intent()
    data object Forward : Intent()
    data class Rename(
        val oldName: String,
        val newName: String,
        val pageType: PageNameData.PageType
    ) : Intent()
}