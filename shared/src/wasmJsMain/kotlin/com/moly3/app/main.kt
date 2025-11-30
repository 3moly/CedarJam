package com.moly3.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.webhistory.withWebHistory
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.RootComponent
import com.moly3.cedarjam.ui.MainApp
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    initApp(AndroidApplicationContext())
    val lifecycle = LifecycleRegistry()
    lifecycle.resume()
    val root: Root = if (true) withWebHistory { stateKeeper, deepLink ->
        RootComponent(
            parentComponentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper
            )
        )
    } else {
        RootComponent(
            parentComponentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = null
            )
        )
    }

    ComposeViewport(document.body!!) {
        MainApp(root)
    }

}
