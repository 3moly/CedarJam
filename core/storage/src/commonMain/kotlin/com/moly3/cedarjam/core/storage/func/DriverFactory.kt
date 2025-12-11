package com.moly3.cedarjam.core.storage.func

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.error.DatabaseError

expect fun createSqlDriver(
    androidApplicationContext: AndroidApplicationContext?,
    dbPath: String,
    schema:SqlSchema<QueryResult.Value<Unit>>
): ResultWrapper<SqlDriver, DatabaseError>