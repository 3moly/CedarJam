package com.moly3.cedarjam.core.storage.func

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.SynchronousFlag
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.ensureNotNull
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.resultBlock
import kotlinx.io.files.Path

actual fun createSqlDriver(
    androidApplicationContext: AndroidApplicationContext?,
    dbPath: String,
    schema: SqlSchema<QueryResult.Value<Unit>>
): ResultWrapper<SqlDriver, DatabaseError> {
    return resultBlock {
        ensureNotNull(dbPath) { DatabaseError.WrongFile("db path is null") }
        val path = Path(dbPath)
        val parentPath = path.parent
        ensureNotNull(parentPath) { DatabaseError.WrongFile("db parent path is null") }
        val extendedConfig = DatabaseConfiguration.Extended(
            foreignKeyConstraints = true,
            basePath = parentPath.toString(),
//            synchronousFlag = SynchronousFlag.FULL,  // Ensures data is written to disk
//            busyTimeout = 1000  // Optional: 5 second timeout for locked database
        )
        NativeSqliteDriver(
            schema,
            path.name,
            onConfiguration = {
                it.copy(extendedConfig = extendedConfig, journalMode = JournalMode.DELETE, inMemory = false)
            }
        )
    }
}