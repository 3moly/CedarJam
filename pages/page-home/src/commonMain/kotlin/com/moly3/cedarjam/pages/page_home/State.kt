package com.moly3.cedarjam.pages.page_home

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.UIState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Stable
data class State(
    val allNodes: UIState<ImmutableList<FileTreeNode>, String> = UIState.Loading,
    val searchTextFieldValue: TextFieldValue = TextFieldValue(""),
    val timeMachinesState: UIState<ImmutableList<TimeMachine>, Nothing> = UIState.Loading
) {
    @Serializable
    data class SaveableState(
        val searchText: String = "",
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                searchTextFieldValue = TextFieldValue(this.searchText),
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                searchText = this.searchTextFieldValue.text,
            )
        }
    }
}