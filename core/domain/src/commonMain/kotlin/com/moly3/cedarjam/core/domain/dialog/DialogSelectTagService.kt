package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.model.TagDTO

class DialogSelectTagService : GlobalDialog<Unit, TagDTO?>(
    isGenericDialog = false,
    closeValue = null
)