package com.moly3.cedarjam.features.feature_graph.func

import com.arkivanov.decompose.router.slot.activate
import com.moly3.cedarjam.features.feature_graph.Intent
import com.moly3.cedarjam.features.feature_graph.model.DialogConfig
import com.moly3.cedarjam.features.feature_graph.model.DialogScope
import com.moly3.cedarjam.features.feature_graph.model.GraphDialog

fun DialogScope.setIsShowGraphDialog(
    target: GraphDialog?,
    isShow: Boolean
) {
    navigation.activate(DialogConfig(target = target))
    val instance = slot.value.child?.instance
    instance?.onIntent(Intent.SetIsShowContent(isShow))
}

fun DialogScope.isGraphDialogInited(): Boolean {
    return slot.value.child?.instance != null
}