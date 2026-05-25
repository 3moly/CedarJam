package com.moly3.cedarjam.di.metro

import com.moly3.cedarjam.navigation.RootFactory
import com.moly3.cedarjam.pages.page_workspace.di.WorkspaceGraph

interface AppGraph {
    val rootComponentFactory: RootFactory
    val cedarJamDependencies: CedarJamDependencies
    val workspaceGraphFactory: WorkspaceGraph.Factory
}
