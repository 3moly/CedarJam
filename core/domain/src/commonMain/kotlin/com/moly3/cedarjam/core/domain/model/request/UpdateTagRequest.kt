package com.moly3.cedarjam.core.domain.model.request

import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.model.TagId

data class UpdateTagRequest(
    val id: TagId,
    val color: Color,
    val modifiedTime: Long
)