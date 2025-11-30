package com.moly3.cedarjam.core.domain.model.request

data class RenameTagRequest(
    val id: Long,
    val newName: String,
    val modifiedTime: Long
)