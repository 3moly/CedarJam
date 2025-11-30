package com.moly3.cedarjam.features.feature_canvas.func

import com.arkivanov.decompose.router.slot.activate
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.features.feature_canvas.Intent
import com.moly3.cedarjam.features.feature_canvas.model.DialogConfig
import com.moly3.cedarjam.features.feature_canvas.model.DialogScope

fun DialogScope.setIsShowGraphDialog(file: FileTreeNode.File, isShow: Boolean) {
    navigation.activate(DialogConfig(file = file))
    val instance = slot.value.child?.instance
    instance?.onIntent(Intent.SetIsShowContent(isShow))
}

fun DialogScope.isGraphDialogInited(): Boolean {
    return slot.value.child?.instance != null
}