package com.recallflow.ui

import androidx.compose.runtime.Immutable
import com.recallflow.domain.Note
import com.recallflow.domain.NoteRepository
import com.recallflow.domain.searchNotes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.random.Random

/** State for the local notes library and its optional editor. */
@Immutable
data class NoteLibraryUiState(
    val notes: List<NoteItemUi> = emptyList(),
    val visibleNotes: List<NoteItemUi> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: NoteLibraryError? = null,
    val validationError: NoteValidationError? = null,
    val editor: NoteEditorUiState? = null,
    val pendingDeleteId: String? = null,
)

@Immutable
data class NoteItemUi(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Immutable
data class NoteEditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val body: String = "",
) {
    val isNew: Boolean get() = noteId == null
}

sealed interface NoteLibraryError {
    data object LoadFailed : NoteLibraryError
    data object SaveFailed : NoteLibraryError
    data object DeleteFailed : NoteLibraryError
}

sealed interface NoteValidationError {
    data object BodyRequired : NoteValidationError
}

sealed interface NoteLibraryEvent {
    data object Initialize : NoteLibraryEvent
    data object Retry : NoteLibraryEvent
    data class SearchQueryChanged(val query: String) : NoteLibraryEvent
    data object NewNoteClicked : NoteLibraryEvent
    data class NoteClicked(val id: String) : NoteLibraryEvent
    data object CloseEditor : NoteLibraryEvent
    data class EditorTitleChanged(val title: String) : NoteLibraryEvent
    data class EditorBodyChanged(val body: String) : NoteLibraryEvent
    data object SaveClicked : NoteLibraryEvent
    data class DeleteClicked(val id: String) : NoteLibraryEvent
    data object DeleteConfirmed : NoteLibraryEvent
    data object DeleteCancelled : NoteLibraryEvent
}

sealed interface NoteLibraryEffect {
    data object NoteSaved : NoteLibraryEffect
    data object NoteDeleted : NoteLibraryEffect
}

/**
 * Coroutine-backed presentation boundary for the notes library.
 *
 * The repository is deliberately injected so Android and desktop can provide
 * their persistent SQLDelight-backed implementation. The controller owns all
 * repository calls and exposes only immutable state and one-shot effects to UI.
 */
class NoteLibraryController(
    private val repository: NoteRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val now: () -> Long = ::currentTimeMillis,
    private val idGenerator: () -> String = {
        "${currentTimeMillis()}-${Random.nextLong().toString(16)}"
    },
) {
    private val _state = MutableStateFlow(NoteLibraryUiState())
    val state: StateFlow<NoteLibraryUiState> = _state.asStateFlow()

    private val _effects = Channel<NoteLibraryEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<NoteLibraryEffect> = _effects.receiveAsFlow()

    private val repositoryMutex = Mutex()

    fun onEvent(event: NoteLibraryEvent) {
        when (event) {
            NoteLibraryEvent.Initialize,
            NoteLibraryEvent.Retry,
            -> refresh()

            is NoteLibraryEvent.SearchQueryChanged -> searchQueryChanged(event.query)
            NoteLibraryEvent.NewNoteClicked -> openNewNote()
            is NoteLibraryEvent.NoteClicked -> openNote(event.id)
            NoteLibraryEvent.CloseEditor -> closeEditor()
            is NoteLibraryEvent.EditorTitleChanged -> editorTitleChanged(event.title)
            is NoteLibraryEvent.EditorBodyChanged -> editorBodyChanged(event.body)
            NoteLibraryEvent.SaveClicked -> saveNote()
            is NoteLibraryEvent.DeleteClicked -> requestDelete(event.id)
            NoteLibraryEvent.DeleteConfirmed -> confirmDelete()
            NoteLibraryEvent.DeleteCancelled -> cancelDelete()
        }
    }

    /** Cancels pending repository work and releases the effect channel. */
    fun close() {
        scope.cancel()
        _effects.close()
    }

    private fun refresh() {
        scope.launch {
            repositoryMutex.withLock {
                _state.update { current ->
                    current.copy(isLoading = true, error = null)
                }

                try {
                    val notes = withContext(ioDispatcher) { repository.listAll() }
                    val items = notes.map(Note::toUi)
                    _state.update { current ->
                        current.withNotes(items).copy(isLoading = false, error = null)
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Throwable) {
                    _state.update { current ->
                        current.copy(isLoading = false, error = NoteLibraryError.LoadFailed)
                    }
                }
            }
        }
    }

    private fun searchQueryChanged(query: String) {
        _state.update { current ->
            current.copy(
                query = query,
                visibleNotes = filterNotes(current.notes, query),
            )
        }
    }

    private fun openNewNote() {
        _state.update { current ->
            current.copy(
                editor = NoteEditorUiState(),
                validationError = null,
                error = null,
                pendingDeleteId = null,
            )
        }
    }

    private fun openNote(id: String) {
        _state.update { current ->
            val note = current.notes.firstOrNull { it.id == id } ?: return@update current
            current.copy(
                editor = NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    body = note.body,
                ),
                validationError = null,
                error = null,
                pendingDeleteId = null,
            )
        }
    }

    private fun closeEditor() {
        _state.update { current ->
            current.copy(editor = null, validationError = null, pendingDeleteId = null)
        }
    }

    private fun editorTitleChanged(title: String) {
        _state.update { current ->
            current.copy(editor = current.editor?.copy(title = title))
        }
    }

    private fun editorBodyChanged(body: String) {
        _state.update { current ->
            current.copy(
                editor = current.editor?.copy(body = body),
                validationError = if (body.isNotBlank()) null else current.validationError,
            )
        }
    }

    private fun saveNote() {
        scope.launch {
            repositoryMutex.withLock {
                val current = _state.value
                val editor = current.editor ?: return@withLock
                if (editor.body.isBlank()) {
                    _state.update { state ->
                        state.copy(validationError = NoteValidationError.BodyRequired)
                    }
                    return@withLock
                }
                if (current.isSaving) return@withLock

                val existing = editor.noteId?.let { id -> current.notes.firstOrNull { it.id == id } }
                val noteId = existing?.id ?: editor.noteId ?: idGenerator()
                val timestamp = now()
                val note = Note(
                    id = noteId,
                    title = editor.title.trim(),
                    body = editor.body.trim(),
                    createdAt = existing?.createdAt ?: timestamp,
                    updatedAt = timestamp,
                )
                _state.update { state ->
                    state.copy(isSaving = true, error = null, validationError = null)
                }

                try {
                    val notes = withContext(ioDispatcher) {
                        if (existing == null) repository.save(note) else repository.update(note)
                        repository.listAll()
                    }
                    val items = notes.map(Note::toUi)
                    _state.update { state ->
                        state.withNotes(items).copy(
                            isSaving = false,
                            error = null,
                            validationError = null,
                            editor = state.editor?.copy(
                                noteId = noteId,
                                title = note.title,
                                body = note.body,
                            ),
                        )
                    }
                    _effects.trySend(NoteLibraryEffect.NoteSaved)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Throwable) {
                    _state.update { state ->
                        state.copy(isSaving = false, error = NoteLibraryError.SaveFailed)
                    }
                }
            }
        }
    }

    private fun requestDelete(id: String) {
        _state.update { current ->
            if (current.notes.any { it.id == id }) {
                current.copy(pendingDeleteId = id, error = null)
            } else {
                current
            }
        }
    }

    private fun cancelDelete() {
        _state.update { current -> current.copy(pendingDeleteId = null) }
    }

    private fun confirmDelete() {
        scope.launch {
            repositoryMutex.withLock {
                val current = _state.value
                val id = current.pendingDeleteId ?: return@withLock
                if (current.isSaving) return@withLock

                _state.update { state ->
                    state.copy(pendingDeleteId = null, isSaving = true, error = null)
                }
                try {
                    val notes = withContext(ioDispatcher) {
                        repository.delete(id)
                        repository.listAll()
                    }
                    val items = notes.map(Note::toUi)
                    _state.update { state ->
                        state.withNotes(items).copy(
                            isSaving = false,
                            error = null,
                            editor = state.editor?.takeUnless { it.noteId == id },
                        )
                    }
                    _effects.trySend(NoteLibraryEffect.NoteDeleted)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Throwable) {
                    _state.update { state ->
                        state.copy(isSaving = false, error = NoteLibraryError.DeleteFailed)
                    }
                }
            }
        }
    }
}

private fun Note.toUi(): NoteItemUi = NoteItemUi(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun NoteItemUi.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun NoteLibraryUiState.withNotes(items: List<NoteItemUi>): NoteLibraryUiState = copy(
    notes = items,
    visibleNotes = filterNotes(items, query),
)

private fun filterNotes(notes: List<NoteItemUi>, query: String): List<NoteItemUi> =
    searchNotes(notes.map(NoteItemUi::toDomain), query).map(Note::toUi)
