package com.recallflow.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.recallflow.data.db.RecallDatabase

class AndroidDatabaseDriverFactory(
    private val context: Context,
    private val databaseName: String = "recallflow.db",
) {
    fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = RecallDatabase.Schema,
        context = context,
        name = databaseName,
    )
}
