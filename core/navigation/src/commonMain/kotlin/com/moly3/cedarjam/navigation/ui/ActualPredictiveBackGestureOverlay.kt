package com.moly3.cedarjam.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackDispatcher

@Composable
expect fun ActualPredictiveBackGestureOverlay(
    backDispatcher: BackDispatcher,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)