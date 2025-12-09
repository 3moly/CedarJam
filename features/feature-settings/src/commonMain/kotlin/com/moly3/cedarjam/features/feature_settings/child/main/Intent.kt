package com.moly3.cedarjam.features.feature_settings.child.main

sealed interface Intent {
    data object Close : Intent
    data object General : Intent
    data object Storage : Intent
    data object Sync : Intent
}