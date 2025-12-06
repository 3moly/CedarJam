package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.dialog.model.DialogTagToTagServiceData

class DialogTagToTagService : GlobalDialog<Unit, DialogTagToTagServiceData?>(
    isGenericDialog = false,
    closeValue = null
)