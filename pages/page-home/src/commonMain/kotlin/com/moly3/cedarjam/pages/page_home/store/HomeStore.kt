package com.moly3.cedarjam.pages.page_home.store

import androidx.compose.ui.text.input.TextFieldValue
import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.pages.page_home.Intent
import com.moly3.cedarjam.pages.page_home.State
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.UIState
import kotlinx.collections.immutable.ImmutableList

internal interface HomeStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetSearchTextFieldValue(val value: TextFieldValue) : Msg
        data class SetCount(val value: Int) : Msg
        data class SetTimes(val value: UIState<ImmutableList<TimeMachine>, Nothing>) : Msg
    }
}
