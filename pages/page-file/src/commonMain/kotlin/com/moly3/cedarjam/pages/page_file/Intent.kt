package com.moly3.cedarjam.pages.page_file

import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest

sealed interface Intent {
    data class PageBack(val file: FileType.PDF) : Intent
    data class PageNext(val file: FileType.PDF) : Intent
    data class ToPage(val file: FileType.PDF, val page: Int) : Intent

    data class ChangeTextNode(val fileNode: FileType.Text, val newText: String) : Intent

    data class SetLinkTag(val value: TagDTO) : Intent
    data class RemoveLinkTag(val value: TagLinkDTO) : Intent
    data class AddAnnotation(val density: Float, val value: CreateAnnotationRequest) : Intent
    data class DeleteAnnotation(val value: AnnotationDTO) : Intent
    data object OpenWorkspaceSettings : Intent
}