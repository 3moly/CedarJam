package com.moly3.cedarjam.core.domain.model.navigation.input

import kotlinx.serialization.Serializable

@Serializable
data class FilePageInput(
    val timestamp: Long,
    val type: FilePageType = FilePageType.Default
){
    @Serializable
    sealed class FilePageType {
        data object Default : FilePageType()
        data object Collection : FilePageType()
    }
}