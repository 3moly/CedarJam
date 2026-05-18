package com.moly3.cedarjam.features.feature_settings.child.storage

sealed interface Intent {
    data object Back : Intent
    data object Close : Intent
}