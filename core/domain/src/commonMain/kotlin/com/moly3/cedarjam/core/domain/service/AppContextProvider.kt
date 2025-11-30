package com.moly3.cedarjam.core.domain.service

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext

class AppContextProvider(
    private val androidApplicationContext: AndroidApplicationContext?,
) {
    fun getApplicationContext(): AndroidApplicationContext? {
        return  androidApplicationContext
    }
}
