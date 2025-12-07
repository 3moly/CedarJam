package com.moly3.cedarjam.features.feature_settings

import kotlinx.serialization.Serializable

data class State(
    val isShowContent: Boolean = true
) {
    @Serializable
    data class SaveableState(
        val isShowContent: Boolean
    )

    companion object {
        fun SaveableState.fromSaveable(): State {
            return State(
                isShowContent = isShowContent
            )
        }

        fun State.toSaveable(): SaveableState {
            return SaveableState(
                isShowContent = isShowContent
            )
        }
    }
}