package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.FileTypeExt
import com.moly3.cedarjam.core.domain.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.toFileType
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingValuesCJ
import com.moly3.cedarjam.core.ui.func.plus
import com.moly3.cedarjam.core.ui.func.wstatusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.TimeMachineList
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import com.moly3.cedarjam.pages.page_home.Intent
import com.moly3.cedarjam.pages.page_home.State
import com.moly3.cedarjam.pages.page_home.model.TimeMachineFilterType
import com.moly3.lazyflow.ui.rememberLazyFlowState
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
internal fun PageContent(
    workspaceFullPath: String,
    state: State,
    onIntent: (Intent) -> Unit
) {

    val scrollState = rememberLazyFlowState()


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
                        (if (state.filterType == TimeMachineFilterType.All) {
                            true
                        } else {
                            when (it) {
                                is TimeMachine.Annotation -> state.filterType == TimeMachineFilterType.Annotation
                                is TimeMachine.Collection -> state.filterType == TimeMachineFilterType.Row
                                is TimeMachine.FileNode -> {
                                    when (it.file.name.extension.toFileType()) {
                                        FileTypeExt.None -> false
                                        FileTypeExt.Pdf -> state.filterType == TimeMachineFilterType.Pdf
                                        FileTypeExt.Image -> state.filterType == TimeMachineFilterType.Image
                                        FileTypeExt.Video -> false
                                        FileTypeExt.Text -> state.filterType == TimeMachineFilterType.Text
                                        FileTypeExt.Mid -> false
                                        FileTypeExt.Canvas -> false
                                    }
                                }

                                is TimeMachine.Row -> state.filterType == TimeMachineFilterType.Row
                                is TimeMachine.Tag -> state.filterType == TimeMachineFilterType.Tag
                            }
                        })
                    }
                }
                val bottomNavPadding = navigationBarsPaddingValuesCJ()
                TimeMachineList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentPadding = bottomNavPadding.plus(
                        layoutDirection = LocalLayoutDirection.current,
                        PaddingValues(vertical = 16.dp)
                    ),
                    workspaceFullPath = workspaceFullPath,
                    scrollState = scrollState,
                    list = filtered,
                    onClick = {
                        onIntent(Intent.OpenTimeMachine(it))
                    }
                )
            }
        }
    }
}