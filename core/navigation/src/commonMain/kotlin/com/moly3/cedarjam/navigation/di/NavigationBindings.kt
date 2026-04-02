package com.moly3.cedarjam.navigation.di

import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.NavigatorDispatcher
import com.moly3.cedarjam.navigation.NavigatorImpl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope

@ContributesTo(AppScope::class)
@BindingContainer
object NavigationBindings {

    // 1. Provide the concrete class as the ONE true Singleton
    @SingleIn(AppScope::class)
    @Provides
    fun provideNavigatorImpl(
        scope: CoroutineScope
    ): NavigatorImpl {
        return NavigatorImpl(scope)
    }

    // 2. Map the first interface to the implementation
    @Provides
    fun provideNavigator(
        impl: NavigatorImpl // Metro automatically grabs the singleton from above
    ): Navigator {
        return impl
    }

    // 3. Map the second interface to the exact same implementation
    @Provides
    fun provideNavigatorDispatcher(
        impl: NavigatorImpl // Metro grabs the exact same singleton again
    ): NavigatorDispatcher {
        return impl
    }
}