package com.moly3.cedarjam.features.feature_graph

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value

interface IDialogGraphContainer {
    val dialogSlot: Value<ChildSlot<*, IDialogGraphComponent>>
    fun setIsShowGraph(isShowMenu: Boolean)
}