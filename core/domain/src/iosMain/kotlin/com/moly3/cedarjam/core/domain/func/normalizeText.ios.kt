package com.moly3.cedarjam.core.domain.func

import platform.Foundation.NSString
import platform.Foundation.precomposedStringWithCanonicalMapping
import platform.Foundation.decomposedStringWithCanonicalMapping

actual fun String.normalizeText(): String {
    // .precomposedStringWithCanonicalMapping converts NFD to NFC
    return (this as NSString).precomposedStringWithCanonicalMapping
}