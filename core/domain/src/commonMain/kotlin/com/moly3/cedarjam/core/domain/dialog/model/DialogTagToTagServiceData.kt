package com.moly3.cedarjam.core.domain.dialog.model

import com.moly3.cedarjam.core.domain.model.TagDTO

data class DialogTagToTagServiceData(
    val firstTag: TagDTO,
    val secondTag: TagDTO
)