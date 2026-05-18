package com.moly3.cedarjam.pages.page_select_workspace.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.func.imePaddingCJ
import com.moly3.cedarjam.core.ui.func.navigationBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.func.statusBarsPaddingCJ
import com.moly3.cedarjam.core.ui.func.windowToolbarPadding
import com.moly3.cedarjam.core.ui.model.WindowSize
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.UIStateContentLazy
import com.moly3.cedarjam.pages.page_select_workspace.ISelectWorkspaceComponent
import com.moly3.cedarjam.pages.page_select_workspace.Intent
import com.moly3.cedarjam.pages.page_select_workspace.State
import com.moly3.cedarjam.pages.page_select_workspace.ui.internal.WorkspaceItem

@Composable
fun SelectWorkspacePage(component: ISelectWorkspaceComponent) {
    val state by component.state.collectAsState(State())
    val windowSize by rememberWindowSize()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPaddingCJ()
            .navigationBarsPaddingCJ()
            .imePaddingCJ()
    ) {
        val screenHeight = maxHeight
        val titleOffset = screenHeight * when (windowSize) {
            WindowSize.Compact -> 0.1f
            WindowSize.Medium -> 0.1f
            WindowSize.Expanded -> 0.1f
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                item {
                    val animatedTextSize by animateFloatAsState(
                        when (windowSize) {
                            WindowSize.Compact -> 24f
                            WindowSize.Medium -> 64f
                            WindowSize.Expanded -> 96f
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = titleOffset),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        CJText(
                            text = "Workspaces.",
                            fontSize = animatedTextSize.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
                UIStateContentLazy(state = state.workspacesState) { workspaces ->
                    items(workspaces) { item ->
                        WorkspaceItem(
                            workspace = item,
                            onIntent = {
                                component.onIntent(it)
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2E4638))
                    .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                    .clickable { component.onIntent(Intent.CreateWorkspace) }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                CJText(
                    text = "Create Workspace",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA5D6A7)
                )
            }
            CJDraggableArea(modifier = Modifier.height(windowToolbarPadding.dp).fillMaxWidth()) { }
        }
    }
}