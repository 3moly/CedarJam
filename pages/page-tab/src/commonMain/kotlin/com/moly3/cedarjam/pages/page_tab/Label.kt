package com.moly3.cedarjam.pages.page_tab

sealed class Label {
    data class ReturnOriginalName(val oldName: String) : Label()
}