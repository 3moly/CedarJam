package com.moly3.cedarjam.pages.page_home.model

data class FileVersionLine(
    val fileRelativePath: String,
    val currentTime: Long?,
    val serverTime: Long?
)