package com.moly3.cedarjam.di.metro

import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.service.MessageServiceImpl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@ContributesTo(AppScope::class)
@BindingContainer
object AppBindings {

    @Provides
    fun provideAlertService(): AlertService {
        return AlertService()
    }

    @Provides
    fun provideMacTrackpadGestureService(): MacTrackpadGestureService {
        return MacTrackpadGestureService()
    }

    @Provides
    fun provideSetIsDarkCoordinator(): CoroutineScope {
        return CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    @Provides
    fun provideMessageService(): IMessageService {
        return MessageServiceImpl()
    }
}