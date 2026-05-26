package com.moly3.cedarjam.android

import android.app.Application
import com.moly3.cedarjam.shared.di.initApp
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        _root_ide_package_.com.moly3.cedarjam.shared.di.initApp(
            this as AndroidApplicationContext,
            isRelease = true
        )
    }
}