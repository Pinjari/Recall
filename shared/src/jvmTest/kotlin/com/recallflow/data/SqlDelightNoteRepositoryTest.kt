package com.recallflow.data

import com.recallflow.domain.Note
import java.nio.file.Files
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SqlDelightNoteRepositoryTest {
    @Test
    fun noteCrudPersistsAcrossDriverReopens() = withDatabasePath { databasePath ->
        val original = note("note-1")
        val updated = original.copy(
            title = "Offline first, updated",
            body = "The local database remains the source of truth.",
            updatedAt = 2L,
        )

        withRepository(databasePath) { repository ->
            assertEquals(emptyList(), repository.listAll())
            repository.save(original)
            assertEquals(original, repository.getById(original.id))
            assertEquals(listOf(original), repository.listAll())
        }

        withRepository(databasePath) { repository ->
            assertEquals(original, repository.getById(original.id))
            repository.update(updated)
            assertEquals(updated, repository.getById(original.id))
        }

        withRepository(databasePath) { repository ->
            assertEquals(listOf(updated), repository.listAll())
            repository.delete(original.id)
            assertNull(repository.getById(original.id))
        }

        withRepository(databasePath) { repository ->
            assertNull(repository.getById(original.id))
            assertEquals(emptyList(), repository.listAll())
        }
    }

    @Test
    fun duplicateIdFailsWithoutReplacingTheOriginal() = withDatabasePath { databasePath ->
        val original = note("note-1")
        withRepository(databasePath) { repository ->
            repository.save(original)
            assertFailsWith<SQLException> {
                repository.save(original.copy(title = "Duplicate"))
            }
            assertEquals(listOf(original), repository.listAll())
        }

        withRepository(databasePath) { repository ->
            assertEquals(original, repository.getById(original.id))
        }
    }

    @Test
    fun listOrderUsesUpdatedTimeDescendingThenIdAscending() = withDatabasePath { databasePath ->
        val older = note("older", updatedAt = 1L)
        val first = note("a", updatedAt = 2L)
        val second = note("b", updatedAt = 2L)
        val newest = note("newest", updatedAt = 3L)

        withRepository(databasePath) { repository ->
            listOf(second, older, newest, first).forEach(repository::save)
            assertEquals(listOf(newest, first, second, older), repository.listAll())
        }

        withRepository(databasePath) { repository ->
            assertEquals(listOf(newest, first, second, older), repository.listAll())
        }
    }

    @Test
    fun updatesPreserveCreationTimeAndMissingMutationsAreNoOps() = withDatabasePath { databasePath ->
        withRepository(databasePath) { repository ->
            val original = note("note-1")
            repository.save(original)
            val updated = original.copy(title = "Updated", createdAt = 99L, updatedAt = 2L)
            repository.update(updated)

            repository.update(note("missing"))
            repository.delete("missing")
            assertNull(repository.getById("missing"))
            assertEquals(listOf(updated.copy(createdAt = original.createdAt)), repository.listAll())
        }
    }

    private fun note(id: String, updatedAt: Long = 1L) = Note(
        id = id,
        title = "Offline first",
        body = "The local database is the source of truth.",
        createdAt = 1L,
        updatedAt = updatedAt,
    )

    private fun withDatabasePath(block: (String) -> Unit) {
        val directory = Files.createTempDirectory("recallflow-db-").toFile()
        try {
            block(directory.resolve("notes.db").path)
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun withRepository(databasePath: String, block: (SqlDelightNoteRepository) -> Unit) {
        val driver = DesktopDatabaseDriverFactory(databasePath).createDriver()
        try {
            block(SqlDelightNoteRepository(driver))
        } finally {
            driver.close()
        }
    }
}
