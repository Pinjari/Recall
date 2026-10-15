package com.recallflow.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.recallflow.data.db.RecallDatabase

class DesktopDatabaseDriverFactory(private val databasePath: String) {
    fun createDriver(): SqlDriver = JdbcSqliteDriver(
        url = "jdbc:sqlite:$databasePath",
        schema = RecallDatabase.Schema,
    )
}
