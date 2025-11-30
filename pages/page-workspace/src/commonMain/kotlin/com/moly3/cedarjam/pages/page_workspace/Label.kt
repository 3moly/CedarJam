package com.moly3.cedarjam.pages.page_workspace

sealed class Label {
    data class ScrollToIndex(val index: Int) : Label()
}