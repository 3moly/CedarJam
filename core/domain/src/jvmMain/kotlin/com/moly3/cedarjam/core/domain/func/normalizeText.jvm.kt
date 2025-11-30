package com.moly3.cedarjam.core.domain.func

import java.text.Normalizer

actual fun String.normalizeText(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFC)
}