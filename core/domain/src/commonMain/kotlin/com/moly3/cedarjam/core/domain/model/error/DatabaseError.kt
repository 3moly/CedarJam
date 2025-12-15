package com.moly3.cedarjam.core.domain.model.error

import androidx.compose.runtime.Stable

@Stable
sealed class DatabaseError {
    data object NotExist : DatabaseError()
    data class WrongFile(
        val message: String
    ) : DatabaseError()
}