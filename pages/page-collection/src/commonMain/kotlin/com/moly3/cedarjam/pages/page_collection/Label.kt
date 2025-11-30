package com.moly3.cedarjam.pages.page_collection

sealed class Label {
    data class ReturnOriginalName(val oldName: String) : Label()
}