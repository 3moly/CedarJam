package com.moly3.cedarjam.pages.page_tab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moly3.cedarjam.navigation.ui.ChildStack2
import com.moly3.cedarjam.pages.page_tab.Intent
import com.moly3.cedarjam.pages.page_tab.Label
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.pages.page_collection.ui.CollectionPage
import com.moly3.cedarjam.pages.page_collection_row.ui.CollectionRowPage
import com.moly3.cedarjam.pages.page_file.ui.FilePage
import com.moly3.cedarjam.pages.page_graph.ui.GraphPage
import com.moly3.cedarjam.pages.page_home.ui.HomePage
import com.moly3.cedarjam.ui.pages.tag.ui.TagPage
import com.moly3.cedarjam.ui.pages.tags.ui.TagsPage
import com.moly3.cedarjam.core.domain.func.formatEpochMillis
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.func.getPageTypeIcon
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.CJTextField2
import com.moly3.cedarjam.core.ui.vectors.ArrowLeft
import com.moly3.cedarjam.core.ui.vectors.ArrowRight
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.tags
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalDecomposeApi::class, ExperimentalTime::class)
@Composable
fun TabPage(component: TabComponent) {
    val tabNameState by component.nameFlow.collectAsState(null)
    val state by component.state.collectAsState()

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
        component.labels.collectLatest { label ->
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
            component.onIntent(
                Intent.Rename(
                    oldName = oldName,
                    newName = newName,
                    pageType = tabName.pageType
                )
            )
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
            .background(LocalAppTheme.current.colors.backgroundSecondary)
    ) {
        Box(Modifier.padding(vertical = 6.dp, horizontal = 12.dp).fillMaxWidth()) {
            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                val (preText, text) = createRefs()
                CJTextField2(
                    modifier = Modifier
                        .widthIn(min = 100.dp)
                        .constrainAs(text) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    iconPainter = tabNameIcon,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start
                    ),
                    text = textNameState,
                    onDone = { onRename() },
                    onLostFocus = { onRename() },
                    enabled = isEditEnabled
                )
                CJText(
                    modifier = Modifier.Companion.constrainAs(preText) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, 8.dp)
                    },
                    text = timeText
                )
            }
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                CJIcon(
                    modifier = Modifier,
                    painter = rememberVectorPainter(ArrowLeft),
                    isEnabled = state.canGoBack,
                    onClick = {
                        component.onIntent(Intent.Back)
                    })
                CJIcon(
                    modifier = Modifier,
                    painter = rememberVectorPainter(ArrowRight),
                    isEnabled = state.canGoForward,
                    onClick = {
                        component.onIntent(Intent.Forward)
                    })
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            val stack by component.childStack.subscribeAsState()
            ChildStack2(
                stack = stack,
                modifier = Modifier.fillMaxSize(),
                animation = stackAnimation()
            ) {
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