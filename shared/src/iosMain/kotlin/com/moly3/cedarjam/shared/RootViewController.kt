package com.moly3.cedarjam.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.ui.ActualPredictiveBackGestureOverlay
import com.moly3.cedarjam.shared.ui.MainApp
import platform.UIKit.UIViewController

@OptIn(ExperimentalDecomposeApi::class)
fun RootViewController(root: Root, backDispatcher: BackDispatcher): UIViewController {
    return ComposeUIViewController {
        ActualPredictiveBackGestureOverlay(
            backDispatcher = backDispatcher,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize()) {
                MainApp(root)
            }
        }
    }
}