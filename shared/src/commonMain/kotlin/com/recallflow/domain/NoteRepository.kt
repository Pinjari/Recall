package com.recallflow.domain

interface NoteRepository {
    fun getById(id: String): Note?

    /** Most recently updated first, with equal timestamps ordered by ID ascending. */
    fun listAll(): List<Note>

    /** Inserts a new note; a duplicate ID fails without replacing the existing note. */
    fun save(note: Note)

    /** Updates an existing note, preserving its creation time. A missing ID is a no-op. */
    fun update(note: Note)

    /** Deletes an existing note. A missing ID is a no-op. */
    fun delete(id: String)
}
