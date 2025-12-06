package com.moly3.cedarjam.core.domain

import kotlinx.serialization.json.Json

val DefaultJson = Json {
    this.ignoreUnknownKeys = true
    prettyPrint = true
//    prettyPrintIndent = "  "
}