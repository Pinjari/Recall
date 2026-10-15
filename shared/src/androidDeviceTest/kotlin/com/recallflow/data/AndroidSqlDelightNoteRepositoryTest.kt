package com.recallflow.data

import android.database.sqlite.SQLiteConstraintException
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.recallflow.domain.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AndroidSqlDelightNoteRepositoryTest {
    @Test
    fun noteCrudAndOrderingPersistAcrossAndroidDriverReopens() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "notes-test-${UUID.randomUUID()}.db"
        val factory = AndroidDatabaseDriverFactory(context, databaseName)
        val original = Note("b", "Offline first", "Stored on Android", 1L, 1L)
        val tied = original.copy(id = "a")
        val updated = original.copy(title = "Updated", body = "Still local", updatedAt = 2L)

        fun withRepository(block: (SqlDelightNoteRepository) -> Unit) {
            val driver = factory.createDriver()
            try {
                assertTrue(driver is AndroidSqliteDriver)
                block(SqlDelightNoteRepository(driver))
            } finally {
                driver.close()
            }
        }

        try {
            withRepository { repository ->
                assertEquals(emptyList<Note>(), repository.listAll())
                repository.save(original)
                repository.save(tied)
                assertEquals(original, repository.getById(original.id))
                assertEquals(listOf(tied, original), repository.listAll())
                assertThrows(SQLiteConstraintException::class.java) {
                    repository.save(original.copy(title = "Duplicate"))
                }
                assertEquals(original, repository.getById(original.id))
            }

            withRepository { repository ->
                assertEquals(listOf(tied, original), repository.listAll())
                repository.update(updated.copy(createdAt = 99L))
                assertEquals(updated, repository.getById(original.id))
                assertEquals(listOf(updated, tied), repository.listAll())
                repository.update(original.copy(id = "missing"))
                repository.delete("missing")
                assertNull(repository.getById("missing"))
            }

            withRepository { repository ->
                assertEquals(listOf(updated, tied), repository.listAll())
                repository.delete(original.id)
                repository.delete(tied.id)
                assertNull(repository.getById(original.id))
                assertEquals(emptyList<Note>(), repository.listAll())
            }

            withRepository { repository ->
                assertEquals(emptyList<Note>(), repository.listAll())
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
