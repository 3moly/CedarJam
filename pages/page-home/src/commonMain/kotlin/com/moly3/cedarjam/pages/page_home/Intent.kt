package com.moly3.cedarjam.pages.page_home

import androidx.compose.ui.text.input.TextFieldValue
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import com.moly3.cedarjam.pages.page_home.model.TimeMachineFilterType

sealed interface Intent {
    data object OpenWorkspaceSettings : Intent
    data object OpenTags : Intent
    data class SetSearchText(val value: TextFieldValue) : Intent
    data class OpenTimeMachine(val timeMachine: TimeMachine) : Intent
    data class OpenTimeMachineType(val value: TimeMachineFilterType) : Intent
}