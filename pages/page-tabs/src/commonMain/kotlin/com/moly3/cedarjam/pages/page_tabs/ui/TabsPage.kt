package com.moly3.cedarjam.pages.page_tabs.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.pages.page_tab.ui.TabPage
import com.moly3.cedarjam.pages.page_tabs.Intent
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.getPageTypeIcon
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import vectors.Add
import vectors.AddRow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun TabsPage(
    modifier: Modifier,
    isActive: Boolean,
    isLastTab: Boolean,
    onSelectedTab: () -> Unit,
    component: TabsComponent
) {
    val state by component.state.collectAsState()
    Row(
        modifier = modifier
            .background(LocalAppTheme.current.colors.backgroundPrimary),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
            for (item in state.tabs) {
                val name by item.nameFlow.collectAsState(null)
                val isSelected = item.index == state.currentTabIndex
                val pageIcon = remember(name) {
                    name?.pageType?.getPageTypeIcon()
                }
                val rawText = when(val name = name?.name){
                    is CJText.Raw -> name.text
                    is CJText.Res -> stringResource(name.res)
                    null -> ""
                }
                TabUI(
                    isActive = isActive,
                    icon = if (pageIcon != null) rememberVectorPainter(pageIcon) else null,
                    isSelected = isSelected,
                    name = rawText,
                    onClick = {
                        component.onIntent(Intent.BringToFrontTab(item.index))
                        onSelectedTab()
                    },
                    onFileTreeReveal = {
                        name?.pageType?.let {
                            component.onIntent(Intent.OnFileReveal(it))
                        }
                    },
                    onRemove = {
                        component.onIntent(Intent.CloseTab(item.index))
                    }
                )
            }
            CJIcon(
                modifier = Modifier,
                painter = rememberVectorPainter(Add),
                isEnabled = true,
                onClick = {
                    component.onIntent(Intent.AddNewTab)
                })
        }
        if (isLastTab) {
            CJIcon(
                modifier = Modifier,
                painter = rememberVectorPainter(AddRow),
                isEnabled = true,
                onClick = {
                    component.onIntent(Intent.AddNewTabs)
                })
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun TabsPageContent(
    modifier: Modifier,
    isLastTab: Boolean,
    isMenuCovered: Boolean,
    component: TabsComponent
) {
    Box(modifier = modifier) {
        val stack by component.children.subscribeAsState()
        ChildStack(
            stack = stack,
            modifier = modifier.fillMaxSize(),
            animation = stackAnimation()
        ) {
            when (val instance = it.instance) {
                is TabsComponent.Child.Tab -> TabPage(tabComponent = instance.component)
            }
        }
        if (!isLastTab) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(5.dp)
                    .fillMaxHeight()
            ) {
                val animWidth by animateDpAsState(if (isMenuCovered) 5.dp else 1.dp)
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(animWidth)
                        .background(LocalAppTheme.current.colors.divide)
                        .clickable {}
                )
            }
        }
    }
}