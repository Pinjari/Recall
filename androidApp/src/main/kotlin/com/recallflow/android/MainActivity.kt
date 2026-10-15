package com.recallflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.cash.sqldelight.db.SqlDriver
import com.recallflow.data.AndroidDatabaseDriverFactory
import com.recallflow.data.SqlDelightNoteRepository
import com.recallflow.ui.RecallFlowScreen

class MainActivity : ComponentActivity() {
    private lateinit var databaseDriver: SqlDriver

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        databaseDriver = AndroidDatabaseDriverFactory(applicationContext).createDriver()
        val repository = SqlDelightNoteRepository(databaseDriver)
        setContent {
            RecallFlowScreen(repository = repository)
        }
    }

    override fun onDestroy() {
        if (::databaseDriver.isInitialized) {
            databaseDriver.close()
        }
        super.onDestroy()
    }
}
