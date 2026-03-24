package com.moly3.cedarjam.core.coordinator.di

import com.moly3.cedarjam.core.coordinator.SetIsDarkCoordinator
import org.koin.dsl.module

fun coordinator() = module {
    single<SetIsDarkCoordinator> {
        SetIsDarkCoordinator()
    }
}