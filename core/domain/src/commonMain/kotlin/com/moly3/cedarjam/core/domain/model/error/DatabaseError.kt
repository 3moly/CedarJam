package com.moly3.cedarjam.core.domain.model.error

sealed class DatabaseError {
    data object NotExist : DatabaseError()
    data class WrongFile(
        val message: String
    ) : DatabaseError()
}