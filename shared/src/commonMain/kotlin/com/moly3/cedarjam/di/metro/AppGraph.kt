package com.moly3.cedarjam.di.metro

import com.moly3.cedarjam.navigation.CreateWorkspaceSession
import com.moly3.cedarjam.navigation.RootFactory

interface AppGraph {
    val rootComponentFactory: RootFactory
    val createWorkspaceSession: CreateWorkspaceSession
    val cedarJamDependencies: CedarJamDependencies
}
