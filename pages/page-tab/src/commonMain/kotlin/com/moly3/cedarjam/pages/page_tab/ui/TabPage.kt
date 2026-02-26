package com.moly3.cedarjam.pages.page_tab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.core.domain.func.formatEpochMillis
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.getPageTypeIcon
import com.moly3.cedarjam.core.ui.func.isCompactUI
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.features.feature_graph.IDialogGraphContainer
import com.moly3.cedarjam.features.feature_graph.model.GraphTabState
import com.moly3.cedarjam.features.feature_graph.ui.ContentNearGraphUI
import com.moly3.cedarjam.pages.page_collection.ui.CollectionPage
import com.moly3.cedarjam.pages.page_collection_row.ui.CollectionRowPage
import com.moly3.cedarjam.pages.page_file.ui.FilePage
import com.moly3.cedarjam.pages.page_graph.ui.GraphPage
import com.moly3.cedarjam.pages.page_home.ui.HomePage
import com.moly3.cedarjam.pages.page_tab.Intent
import com.moly3.cedarjam.pages.page_tab.Label
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.pages.tag.ui.TagPage
import com.moly3.cedarjam.ui.pages.tags.ui.TagsPage
import com.moly3.cedarjam.ui.tags
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalDecomposeApi::class, ExperimentalTime::class)
@Composable
fun TabPage(tabComponent: TabComponent) {
    val tabNameState by tabComponent.nameFlow.collectAsState(null)
    val state by tabComponent.state.collectAsState()

    //TODO: Cannot use rememberTextFieldState() in wasmJs because it relies on rememberSaveable,
    // which is not supported during state restoration. Using a plain TextFieldState instead.
    val textNameState = remember { TextFieldState() }

    val tabNameIcon = remember(tabNameState) {
        tabNameState?.pageType?.getPageTypeIcon()
    }
    val timeText = remember(tabNameState) {
        tabNameState?.modifiedTime?.formatEpochMillis(isShowTime = true) ?: ""
    }
    val isEditEnabled = remember(tabNameState) {
        tabNameState?.isEditEnabled() ?: false
    }
    LaunchedEffect(tabNameState) {
        getString(Res.string.tags)
        val rawText = when (val name = tabNameState?.name) {
            is CJText.Raw -> name.text
            is CJText.Res -> getString(name.res)
            null -> ""
        }
        textNameState.setTextAndPlaceCursorAtEnd(rawText)
    }
    LaunchedEffect(Unit) {
        tabComponent.labels.collectLatest { label ->
            when (label) {
                is Label.ReturnOriginalName -> {
                    println("ReturnOriginalName ${label.oldName}")
                    textNameState.setTextAndPlaceCursorAtEnd(label.oldName)
                }
            }
        }
    }
    fun onRename() {
        val newName = textNameState.text.toString()
        val tabName = tabNameState
        if (tabName != null) {
            val oldName = when (val name = tabName.name) {
                is CJText.Raw -> name.text
                is CJText.Res -> "-- will not change"
            }
            tabComponent.onIntent(
                Intent.Rename(
                    oldName = oldName,
                    newName = newName,
                    pageType = tabName.pageType
                )
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppTheme.current.colors.backgroundSecondary)
    ) {
        if (!isCompactUI()) {
            TabControlsContent(
                tabNameIcon = tabNameIcon,
                canGoBack = state.canGoBack,
                canGoForward = state.canGoForward,
                textNameState = textNameState,
                timeText = timeText,
                isEditEnabled = isEditEnabled,
                onRename = {
                    onRename()
                },
                onBack = {
                    tabComponent.onIntent(Intent.Back)
                },
                onForward = {
                    tabComponent.onIntent(Intent.Forward)
                }
            )
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            val stack by tabComponent.children.subscribeAsState()
            ChildStack(
                stack = stack,
                modifier = Modifier.fillMaxSize(),
                animation = stackAnimation()
            ) {
                val component = it.instance.component
                if (component is IDialogGraphContainer) {
                    val graphTabState = remember(state.canGoBack,state.canGoBack){
                        GraphTabState(
                            canGoBack = state.canGoBack,
                            canGoForward = state.canGoForward,
                            goBack = {
                                tabComponent.onIntent(Intent.Back)
                            },
                            goForward = {
                                tabComponent.onIntent(Intent.Forward)
                            }
                        )
                    }
                    ContentNearGraphUI(
                        state = graphTabState,
                        mainContent = {
                            when (val instance = it.instance) {
                                is TabComponent.Child.Home -> HomePage(component = instance.component)
                                is TabComponent.Child.Graph -> GraphPage(component = instance.component)
                                is TabComponent.Child.File -> FilePage(component = instance.component)
                                is TabComponent.Child.CollectionRow -> CollectionRowPage(component = instance.component)
                                is TabComponent.Child.Collection -> CollectionPage(component = instance.component)
                                is TabComponent.Child.Tags -> TagsPage(component = instance.component)
                                is TabComponent.Child.Tag -> TagPage(component = instance.component)
                            }
                        },
                        dialogSlot = component.dialogSlot,
                        setIsShowGraph = { component.setIsShowGraph(it) }
                    )
                } else {
                    when (val instance = it.instance) {
                        is TabComponent.Child.Home -> HomePage(component = instance.component)
                        is TabComponent.Child.Graph -> GraphPage(component = instance.component)
                        is TabComponent.Child.File -> FilePage(component = instance.component)
                        is TabComponent.Child.CollectionRow -> CollectionRowPage(component = instance.component)
                        is TabComponent.Child.Collection -> CollectionPage(component = instance.component)
                        is TabComponent.Child.Tags -> TagsPage(component = instance.component)
                        is TabComponent.Child.Tag -> TagPage(component = instance.component)
                    }
                }
            }
        }
    }
}