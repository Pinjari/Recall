package com.recallflow.desktop

import com.recallflow.domain.Note
import com.recallflow.domain.NoteRepository
import com.recallflow.ui.NoteLibraryController
import com.recallflow.ui.NoteLibraryEvent
import com.recallflow.ui.NoteValidationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class NoteLibraryControllerTest {
    @Test
    fun controllerCreatesUpdatesDeletesAndReloadsNotes() {
        val repository = InMemoryNoteRepository()
        var timestamp = 1L
        val controller = controller(repository, now = { timestamp })

        try {
            controller.onEvent(NoteLibraryEvent.Initialize)
            assertFalse(controller.state.value.isLoading)

            controller.onEvent(NoteLibraryEvent.NewNoteClicked)
            controller.onEvent(NoteLibraryEvent.EditorTitleChanged("First note"))
            controller.onEvent(NoteLibraryEvent.EditorBodyChanged("Keep this locally."))
            controller.onEvent(NoteLibraryEvent.SaveClicked)

            val created = repository.getById("note-1")
            assertEquals(Note("note-1", "First note", "Keep this locally.", 1L, 1L), created)
            assertEquals(listOf("note-1"), controller.state.value.notes.map { it.id })

            timestamp = 2L
            controller.onEvent(NoteLibraryEvent.NoteClicked("note-1"))
            controller.onEvent(NoteLibraryEvent.EditorBodyChanged("Updated while offline."))
            controller.onEvent(NoteLibraryEvent.SaveClicked)

            assertEquals(
                Note("note-1", "First note", "Updated while offline.", 1L, 2L),
                repository.getById("note-1"),
            )

            controller.onEvent(NoteLibraryEvent.DeleteClicked("note-1"))
            assertEquals("note-1", controller.state.value.pendingDeleteId)
            controller.onEvent(NoteLibraryEvent.DeleteConfirmed)

            assertNull(repository.getById("note-1"))
            assertEquals(emptyList(), controller.state.value.notes)
        } finally {
            controller.close()
        }

        val reopenedController = controller(repository, now = { 3L })
        try {
            reopenedController.onEvent(NoteLibraryEvent.Initialize)
            assertEquals(emptyList(), reopenedController.state.value.notes)
        } finally {
            reopenedController.close()
        }
    }

    @Test
    fun controllerRequiresBodyBeforeSaving() {
        val repository = InMemoryNoteRepository()
        val controller = controller(repository)

        try {
            controller.onEvent(NoteLibraryEvent.Initialize)
            controller.onEvent(NoteLibraryEvent.NewNoteClicked)
            controller.onEvent(NoteLibraryEvent.SaveClicked)

            assertEquals(NoteValidationError.BodyRequired, controller.state.value.validationError)
            assertEquals(emptyList(), repository.listAll())
        } finally {
            controller.close()
        }
    }

    private fun controller(
        repository: NoteRepository,
        now: () -> Long = { 1L },
    ): NoteLibraryController = NoteLibraryController(
        repository = repository,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        ioDispatcher = Dispatchers.Unconfined,
        now = now,
        idGenerator = { "note-1" },
    )

    private class InMemoryNoteRepository : NoteRepository {
        private val notes = linkedMapOf<String, Note>()

        override fun getById(id: String): Note? = notes[id]

        override fun listAll(): List<Note> = notes.values.sortedWith(
            compareByDescending<Note> { it.updatedAt }.thenBy { it.id },
        )

        override fun save(note: Note) {
            check(notes.putIfAbsent(note.id, note) == null) { "Duplicate note ID" }
        }

        override fun update(note: Note) {
            notes[note.id]?.let { existing ->
                notes[note.id] = note.copy(createdAt = existing.createdAt)
            }
        }

        override fun delete(id: String) {
            notes.remove(id)
        }
    }
}
