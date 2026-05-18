package com.moly3.cedarjam.features.feature_settings.ui.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.features.feature_settings.IDialogSettingsComponent.Child
import com.moly3.cedarjam.features.feature_settings.Intent
import com.moly3.cedarjam.features.feature_settings.State
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
internal fun DialogCanvasUIContent(
    childStack: Value<ChildStack<*, Child>>,
    state: State,
    onIntent: (Intent) -> Unit
) {
    if (state.isShowContent) {

        Box(modifier = Modifier) {

        }
    }
}