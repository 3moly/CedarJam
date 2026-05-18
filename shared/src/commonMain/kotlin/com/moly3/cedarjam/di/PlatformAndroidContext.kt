package com.moly3.cedarjam.di

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext

object PlatformAndroidContext {
    lateinit var applicationContext: AndroidApplicationContext
        private set

    fun init(context: AndroidApplicationContext) {
        applicationContext = context
    }
}
