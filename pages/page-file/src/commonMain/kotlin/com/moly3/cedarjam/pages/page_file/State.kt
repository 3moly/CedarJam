package com.moly3.cedarjam.pages.page_file

import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import kotlinx.serialization.Serializable

data class State(
    val connectionsCount: Int = 0,
    val relativePath: String = "",
    val fileType: FileType? = null,
    val tags: List<TagDTO> = listOf(),
    val tagLinks: List<TagLinkDTO> = listOf()
) {
    @Serializable
    data class SaveableState(
        val fileType: FileType? = null
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                fileType = fileType
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                fileType = fileType
            )
        }
    }
}