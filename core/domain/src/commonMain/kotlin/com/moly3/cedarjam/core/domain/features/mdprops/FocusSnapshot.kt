package com.moly3.cedarjam.core.domain.features.mdprops

data class FocusSnapshot(
    val rowId: String?,
    val caret: RowFocusManager.CaretTarget = RowFocusManager.CaretTarget.End,
)