package com.moly3.cedarjam.shared.di

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext

object PlatformAndroidContext {
    lateinit var applicationContext: AndroidApplicationContext
        private set

    fun init(context: AndroidApplicationContext) {
        applicationContext = context
    }
}
