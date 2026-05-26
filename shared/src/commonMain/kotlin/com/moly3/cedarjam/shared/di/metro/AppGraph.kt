package com.moly3.cedarjam.shared.di.metro

import com.moly3.cedarjam.shared.navigation.RootFactory
import com.moly3.cedarjam.pages.page_workspace.di.WorkspaceGraph

interface AppGraph {
    val rootComponentFactory: com.moly3.cedarjam.shared.navigation.RootFactory
    val cedarJamDependencies: com.moly3.cedarjam.shared.di.metro.CedarJamDependencies
    val workspaceGraphFactory: WorkspaceGraph.Factory
}
