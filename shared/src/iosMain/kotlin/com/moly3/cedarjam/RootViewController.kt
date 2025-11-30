package com.moly3.cedarjam

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.ui.MainApp
import platform.UIKit.UIViewController

fun RootViewController(root: Root, backDispatcher: BackDispatcher): UIViewController {
    return ComposeUIViewController {
        MainApp(root)
    }
}