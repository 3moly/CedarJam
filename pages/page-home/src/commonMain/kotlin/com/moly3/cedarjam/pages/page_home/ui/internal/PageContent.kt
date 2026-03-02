package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.pages.page_home.Intent
import com.moly3.cedarjam.pages.page_home.State
import com.moly3.cedarjam.pages.page_home.func.dpSize
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import com.moly3.cedarjam.pages.page_home.model.TimeMachineFilterType
import com.moly3.lazyFlow.func.items
import com.moly3.lazyFlow.model.FlowItemSizeMode
import com.moly3.lazyFlow.ui.LazyFlow
import com.moly3.lazyflow.core.model.FlowOrientation
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
internal fun PageContent(
    workspaceFullPath: String,
    state: State,
    onIntent: (Intent) -> Unit
) {
    val rowSize = remember { mutableStateOf<IntSize>(IntSize.Zero) }
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val size1 = remember(density) {
        DpSize(150.dp, 150.dp)
    }
    val size2 = remember(density) {
        DpSize(250.dp, 150.dp)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CJSearchTextField(
            modifier = Modifier
                .wstatusBarsPaddingCJ()
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            isSearchIcon = true,
            placeholderText = "Search...",
            value = state.searchTextFieldValue,
            onValueChange = {
                onIntent(Intent.SetSearchText(it))
            }
        )
        FiltersUI(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp),
            value = state.filterType
        ) {
            onIntent(Intent.OpenTimeMachineType(it))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            UIStateContentNoBox(state = state.timeMachinesState) { timeMachines ->
                val filtered = remember(timeMachines, state.filterType) {
                    timeMachines.filter {
                        if (state.filterType == TimeMachineFilterType.All) {
                            true
                        } else {
                            when (it) {
                                is TimeMachine.Annotation -> state.filterType == TimeMachineFilterType.Annotation
                                is TimeMachine.Collection -> state.filterType == TimeMachineFilterType.Row
                                is TimeMachine.FileNode -> {

                                    when (it.file.name.extension) {
                                        "md", "txt" -> state.filterType == TimeMachineFilterType.Text
                                        "pdf" -> state.filterType == TimeMachineFilterType.Pdf
                                        "png", "jpeg", "gif", "wbep", "jpg" -> state.filterType == TimeMachineFilterType.Image
                                        "canvas" -> state.filterType == TimeMachineFilterType.Canvas
                                        else -> false
                                    }
                                }

                                is TimeMachine.Row -> state.filterType == TimeMachineFilterType.Row
                                is TimeMachine.Tag -> state.filterType == TimeMachineFilterType.Tag
                            }
                        }
                    }
                }
                LazyFlow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    scrollState = scrollState,
                    afterScrollModifier = Modifier.padding(vertical = 16.dp)
                        .navigationBarsPaddingCJ()
                        .onGloballyPositioned({
                            rowSize.value = it.size
                        }),
                    orientation = FlowOrientation.Row,
                    horizontalGap = 8.dp,
                    verticalGap = 8.dp
                ) {
                    items(items = filtered, key = { it }, itemSize = {
                        val dpSize = when (it) {
                            is TimeMachine.Annotation -> size2
                            is TimeMachine.Collection -> size1
                            is TimeMachine.FileNode -> size1
                            is TimeMachine.Row -> size1
                            is TimeMachine.Tag -> size2
                        }
                        FlowItemSizeMode.Exact(dpSize)
                    }) {
                        val modifier = Modifier.size(it.dpSize())
                        TimeMachineItem(
                            modifier = modifier,
                            item = it,
                            onIntent = onIntent,
                            workspaceFullPath = workspaceFullPath
                        )
                    }
                }
            }
        }
    }
}