package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.model.TagDTO

class DialogTagToTagService : GlobalDialog<Unit, DialogTagToTagServiceData?>(
    isGenericDialog = false,
    closeValue = null
)

data class DialogTagToTagServiceData(
    val firstTag: TagDTO,
    val secondTag: TagDTO
)