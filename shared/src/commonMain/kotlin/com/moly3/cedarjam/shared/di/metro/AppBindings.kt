package com.moly3.cedarjam.shared.di.metro

import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.shared.service.MessageServiceImpl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object AppBindings {

    @SingleIn(AppScope::class)
    @Provides
    fun provideAlertService(): AlertService = AlertService()

    @SingleIn(AppScope::class)
    @Provides
    fun provideMacTrackpadGestureService(): MacTrackpadGestureService =
        MacTrackpadGestureService()

    @SingleIn(AppScope::class)
    @Provides
    fun provideMessageService(): IMessageService =
        MessageServiceImpl()
}
