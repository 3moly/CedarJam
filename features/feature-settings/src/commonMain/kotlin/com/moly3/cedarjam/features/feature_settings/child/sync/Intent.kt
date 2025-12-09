package com.moly3.cedarjam.features.feature_settings.child.sync

sealed interface Intent {
    data object Back : Intent
    data object Close : Intent
    data object Sync : Intent
}