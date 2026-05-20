package com.moly3.cedarjam.di.metro

import com.moly3.cedarjam.navigation.RootFactory

interface AppGraph {
    val rootComponentFactory: RootFactory
    val cedarJamDependencies: CedarJamDependencies
}
