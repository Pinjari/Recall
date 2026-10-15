package com.recallflow.data

import app.cash.sqldelight.db.SqlDriver
import com.recallflow.data.db.RecallDatabase
import com.recallflow.domain.Note
import com.recallflow.domain.NoteRepository

class SqlDelightNoteRepository(driver: SqlDriver) : NoteRepository {
    private val queries = RecallDatabase(driver).notesQueries

    override fun getById(id: String): Note? = queries
        .selectById(id)
        .executeAsOneOrNull()
        ?.toDomain()

    override fun listAll(): List<Note> = queries
        .selectAll()
        .executeAsList()
        .map { it.toDomain() }

    override fun save(note: Note) {
        queries.insertNote(
            id = note.id,
            title = note.title,
            body = note.body,
            created_at = note.createdAt,
            updated_at = note.updatedAt,
        )
    }

    override fun update(note: Note) {
        queries.updateNote(
            title = note.title,
            body = note.body,
            updated_at = note.updatedAt,
            id = note.id,
        )
    }

    override fun delete(id: String) {
        queries.deleteNote(id)
    }
}

private fun com.recallflow.data.db.Notes.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    createdAt = created_at,
    updatedAt = updated_at,
)
