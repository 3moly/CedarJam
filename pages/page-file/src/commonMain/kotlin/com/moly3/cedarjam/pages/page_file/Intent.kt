package com.moly3.cedarjam.pages.page_file

import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO

sealed interface Intent {
    data class SetIsShowGraph(val value: Boolean) : Intent
    data class PageBack(val file: FileType.PDF) : Intent
    data class PageNext(val file: FileType.PDF) : Intent
    data class ToPage(val file: FileType.PDF, val page: Int) : Intent

    data class ChangeTextNode(val fileNode: FileType.Text, val newText: String) : Intent

    data class SetLinkTag(val value: TagDTO) : Intent
    data class RemoveLinkTag(val value: TagLinkDTO) : Intent
}