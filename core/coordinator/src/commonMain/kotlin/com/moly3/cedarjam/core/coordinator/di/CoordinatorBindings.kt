package com.moly3.cedarjam.core.coordinator.di

import com.moly3.cedarjam.core.coordinator.SetIsDarkCoordinator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object CoordinatorBindings {

    @SingleIn(AppScope::class)
    @Provides
    fun provideSetIsDarkCoordinator(): SetIsDarkCoordinator {
        return SetIsDarkCoordinator()
    }
}