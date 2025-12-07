package com.moly3.cedarjam.features.feature_settings.func

import com.moly3.cedarjam.features.feature_settings.model.DialogScope


fun DialogScope.isGraphDialogInited(): Boolean {
    return slot.value.child?.instance != null
}