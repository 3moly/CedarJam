package com.moly3.cedarjam.pages.page_home

import androidx.compose.ui.text.input.TextFieldValue
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.UIState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

data class State(
    val searchTextFieldValue: TextFieldValue = TextFieldValue(""),
    val count: Int = 0,
    val timeMachinesState: UIState<ImmutableList<TimeMachine>, Nothing> = UIState.Loading,


) {
    @Serializable
    data class SaveableState(
        val searchText: String = "",
        val count: Int = 0,
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                searchTextFieldValue = TextFieldValue(this.searchText),
                count = this.count,

                )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                searchText = this.searchTextFieldValue.text,
                count = this.count
            )
        }
    }
}