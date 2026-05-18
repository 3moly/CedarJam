package com.moly3.cedarjam.core.domain.model

enum class SyncStatus(val code: Long) {
    SYNCED(0),
    DIRTY(1),
    NEW(2),
    DELETED(3)
}