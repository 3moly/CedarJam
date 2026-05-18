package com.moly3.cedarjam.pages.page_home

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.pages.page_home.model.TimeMachineFilterType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Stable
data class State(
    val allNodes: UIState<ImmutableList<FileTreeNode>, String> = UIState.Loading,
    val searchTextFieldValue: TextFieldValue = TextFieldValue(""),
    val timeMachinesState: UIState<ImmutableList<TimeMachine>, Nothing> = UIState.Loading,
    val filterType: TimeMachineFilterType = TimeMachineFilterType.All
) {
    @Serializable
    data class SaveableState(
        val searchText: String = "",
        val filterType: TimeMachineFilterType = TimeMachineFilterType.All
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                searchTextFieldValue = TextFieldValue(this.searchText),
                filterType = this.filterType
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                searchText = this.searchTextFieldValue.text,
                filterType = this.filterType
            )
        }
    }
}