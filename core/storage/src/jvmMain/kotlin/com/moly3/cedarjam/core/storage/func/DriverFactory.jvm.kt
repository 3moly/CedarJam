package com.moly3.cedarjam.core.storage.func

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.resultBlock
import java.util.Properties

actual fun createSqlDriver(
    androidApplicationContext: AndroidApplicationContext?,
    dbPath: String,
    schema: SqlSchema<QueryResult.Value<Unit>>
): ResultWrapper<SqlDriver, DatabaseError> {
    return resultBlock {
        if (dbPath == null) {
            val dr = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            schema.create(dr)
            dr
        } else {
            try {
                val jdbc = JdbcSqliteDriver(
                    "jdbc:sqlite:$dbPath",
                    properties = Properties(1).apply {
                        put("journal_mode", "DELETE")
                        put("busy_timeout", "60000") // 60 seconds
                        put("synchronous", "NORMAL")
                        put("foreign_keys", "ON")
                    },
                    schema
                )
                jdbc
            } catch (exc: Exception) {
                raise(DatabaseError.WrongFile(exc.message ?: "Unknown SQLite error"))
            }
        }
    }
}