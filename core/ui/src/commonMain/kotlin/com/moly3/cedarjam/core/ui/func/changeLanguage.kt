package com.moly3.cedarjam.core.ui.func

import io.github.sudarshanmhasrup.localina.api.LocaleUpdater

fun changeLanguage(locale: String) {
    LocaleUpdater.updateLocale(locale)
}