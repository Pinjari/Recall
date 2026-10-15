package com.recallflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.recallflow.domain.NoteRepository
import com.recallflow.ui.resources.Res
import com.recallflow.ui.resources.app_name
import com.recallflow.ui.resources.body_required
import com.recallflow.ui.resources.brand_monogram
import com.recallflow.ui.resources.cancel
import com.recallflow.ui.resources.clear_search
import com.recallflow.ui.resources.close_editor
import com.recallflow.ui.resources.confirm_delete
import com.recallflow.ui.resources.create_first_note
import com.recallflow.ui.resources.delete_confirm_body
import com.recallflow.ui.resources.delete_confirm_title
import com.recallflow.ui.resources.delete_note
import com.recallflow.ui.resources.empty_notes_body
import com.recallflow.ui.resources.empty_notes_heading
import com.recallflow.ui.resources.error_delete
import com.recallflow.ui.resources.error_load
import com.recallflow.ui.resources.error_save
import com.recallflow.ui.resources.library_heading
import com.recallflow.ui.resources.library_subheading
import com.recallflow.ui.resources.loading_notes
import com.recallflow.ui.resources.new_note
import com.recallflow.ui.resources.no_results_body
import com.recallflow.ui.resources.no_results_heading
import com.recallflow.ui.resources.note_body_label
import com.recallflow.ui.resources.note_body_placeholder
import com.recallflow.ui.resources.note_title_label
import com.recallflow.ui.resources.notes_matching
import com.recallflow.ui.resources.notes_visible
import com.recallflow.ui.resources.recent_notes
import com.recallflow.ui.resources.retry
import com.recallflow.ui.resources.save_note
import com.recallflow.ui.resources.saving_note
import com.recallflow.ui.resources.saved_message
import com.recallflow.ui.resources.deleted_message
import com.recallflow.ui.resources.search_hint
import com.recallflow.ui.resources.select_note_body
import com.recallflow.ui.resources.select_note_heading
import com.recallflow.ui.resources.untitled_note
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val LightColors = lightColorScheme(
    primary = Color(0xFF245E4B),
    onPrimary = Color.White,
    background = Color(0xFFF5F5EF),
    onBackground = Color(0xFF202B25),
    surface = Color(0xFFFFFEF9),
    onSurface = Color(0xFF202B25),
    surfaceVariant = Color(0xFFE3EBE1),
    onSurfaceVariant = Color(0xFF46554C),
    outlineVariant = Color(0xFFD3DCD1),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA2D4B9),
    onPrimary = Color(0xFF123B2C),
    background = Color(0xFF141C18),
    onBackground = Color(0xFFE1E9DF),
    surface = Color(0xFF1D2821),
    onSurface = Color(0xFFE1E9DF),
    surfaceVariant = Color(0xFF2C3B31),
    onSurfaceVariant = Color(0xFFBACBBE),
    outlineVariant = Color(0xFF405147),
)

/** Entry point used by both platform launchers. */
@Composable
fun RecallFlowScreen(
    repository: NoteRepository,
    modifier: Modifier = Modifier,
) {
    val controller = remember(repository) { NoteLibraryController(repository) }
    val state by controller.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.saved_message)
    val deletedMessage = stringResource(Res.string.deleted_message)

    LaunchedEffect(controller) {
        controller.onEvent(NoteLibraryEvent.Initialize)
        controller.effects.collect { effect ->
            val message = when (effect) {
                NoteLibraryEffect.NoteSaved -> savedMessage
                NoteLibraryEffect.NoteDeleted -> deletedMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    DisposableEffect(controller) {
        onDispose(controller::close)
    }

    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors) {
        Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                NoteLibraryContent(
                    state = state,
                    onEvent = controller::onEvent,
                    modifier = Modifier.fillMaxSize(),
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

/** Stateless content kept separate so the UI can be exercised with fake state/repositories. */
@Composable
fun NoteLibraryContent(
    state: NoteLibraryUiState,
    onEvent: (NoteLibraryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .widthIn(max = 1120.dp)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LibraryHeader(
                enabled = !state.isSaving,
                onNewNote = { onEvent(NoteLibraryEvent.NewNoteClicked) },
            )
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(NoteLibraryEvent.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.search_hint)) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        TextButton(onClick = { onEvent(NoteLibraryEvent.SearchQueryChanged("")) }) {
                            Text(stringResource(Res.string.clear_search))
                        }
                    }
                } else {
                    null
                },
            )
            Text(
                text = searchSummary(state),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )

            if (state.error != null &&
                (state.notes.isNotEmpty() || state.editor != null ||
                    state.error != NoteLibraryError.LoadFailed)
            ) {
                ErrorBanner(
                    error = state.error,
                    onRetry = { onEvent(NoteLibraryEvent.Retry) },
                )
            }
            if (state.isLoading && state.notes.isNotEmpty()) {
                LinearLoadingIndicator()
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val isWide = maxWidth >= 780.dp
                when {
                    state.isLoading && state.notes.isEmpty() -> LoadingPane()
                    state.error == NoteLibraryError.LoadFailed &&
                        state.notes.isEmpty() &&
                        state.editor == null -> ErrorPane(
                        onRetry = { onEvent(NoteLibraryEvent.Retry) },
                    )
                    !isWide && state.editor != null -> NoteEditorPane(
                        editor = state.editor,
                        isSaving = state.isSaving,
                        validationError = state.validationError,
                        onEvent = onEvent,
                    )
                    isWide -> WideLibraryPane(state = state, onEvent = onEvent)
                    else -> NotesPane(state = state, onEvent = onEvent)
                }
            }
        }
    }

    val pendingDelete = state.pendingDeleteId?.let { id -> state.notes.firstOrNull { it.id == id } }
    if (pendingDelete != null) {
        DeleteConfirmationDialog(
            note = pendingDelete,
            onConfirm = { onEvent(NoteLibraryEvent.DeleteConfirmed) },
            onDismiss = { onEvent(NoteLibraryEvent.DeleteCancelled) },
        )
    }
}

@Composable
private fun LibraryHeader(
    enabled: Boolean,
    onNewNote: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.brand_monogram),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(Res.string.app_name),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(Res.string.library_subheading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Button(onClick = onNewNote, enabled = enabled) {
            Text(stringResource(Res.string.new_note))
        }
    }
}

@Composable
private fun WideLibraryPane(
    state: NoteLibraryUiState,
    onEvent: (NoteLibraryEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NotesPane(
            state = state,
            onEvent = onEvent,
            modifier = Modifier.weight(0.42f),
        )
        Surface(
            modifier = Modifier.weight(0.58f).fillMaxHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            if (state.editor == null) {
                SelectNotePane()
            } else {
                NoteEditorPane(
                    editor = state.editor,
                    isSaving = state.isSaving,
                    validationError = state.validationError,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun NotesPane(
    state: NoteLibraryUiState,
    onEvent: (NoteLibraryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        when {
            state.notes.isEmpty() -> EmptyNotesPane(
                onCreate = { onEvent(NoteLibraryEvent.NewNoteClicked) },
            )
            state.visibleNotes.isEmpty() -> NoResultsPane(
                onClear = { onEvent(NoteLibraryEvent.SearchQueryChanged("")) },
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(contentType = "header") {
                    Text(
                        text = stringResource(Res.string.recent_notes),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(
                    items = state.visibleNotes,
                    key = { note -> note.id },
                    contentType = { "note" },
                ) { note ->
                    NoteCard(
                        note = note,
                        enabled = !state.isSaving,
                        onOpen = { onEvent(NoteLibraryEvent.NoteClicked(note.id)) },
                        onDelete = { onEvent(NoteLibraryEvent.DeleteClicked(note.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteItemUi,
    enabled: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpen,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = note.title.ifBlank { stringResource(Res.string.untitled_note) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = note.body.replace('\n', ' '),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onDelete, enabled = enabled) {
                Text(stringResource(Res.string.delete_note))
            }
        }
    }
}

@Composable
private fun NoteEditorPane(
    editor: NoteEditorUiState,
    isSaving: Boolean,
    validationError: NoteValidationError?,
    onEvent: (NoteLibraryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (editor.isNew) {
                        stringResource(Res.string.new_note)
                    } else {
                        stringResource(Res.string.library_heading)
                    },
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(
                    onClick = { onEvent(NoteLibraryEvent.CloseEditor) },
                    enabled = !isSaving,
                ) {
                    Text(stringResource(Res.string.close_editor))
                }
            }
            OutlinedTextField(
                value = editor.title,
                onValueChange = { onEvent(NoteLibraryEvent.EditorTitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                singleLine = true,
                label = { Text(stringResource(Res.string.note_title_label)) },
            )
            OutlinedTextField(
                value = editor.body,
                onValueChange = { onEvent(NoteLibraryEvent.EditorBodyChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp),
                enabled = !isSaving,
                minLines = 8,
                label = { Text(stringResource(Res.string.note_body_label)) },
                placeholder = { Text(stringResource(Res.string.note_body_placeholder)) },
                isError = validationError != null,
                supportingText = if (validationError != null) {
                    { Text(stringResource(Res.string.body_required)) }
                } else {
                    null
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (editor.noteId != null) {
                    TextButton(
                        onClick = { onEvent(NoteLibraryEvent.DeleteClicked(editor.noteId)) },
                        enabled = !isSaving,
                    ) {
                        Text(stringResource(Res.string.delete_note))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onEvent(NoteLibraryEvent.SaveClicked) },
                    enabled = !isSaving,
                ) {
                    Text(
                        stringResource(
                            if (isSaving) Res.string.saving_note else Res.string.save_note,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectNotePane() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.select_note_heading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(Res.string.select_note_body),
            modifier = Modifier.widthIn(max = 360.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun LoadingPane() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(Res.string.loading_notes),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun LinearLoadingIndicator() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .heightIn(min = 4.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun ErrorPane(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.error_load),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Button(onClick = onRetry) {
            Text(stringResource(Res.string.retry))
        }
    }
}

@Composable
private fun EmptyNotesPane(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.empty_notes_heading),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(Res.string.empty_notes_body),
            modifier = Modifier.widthIn(max = 380.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onCreate) {
            Text(stringResource(Res.string.create_first_note))
        }
    }
}

@Composable
private fun NoResultsPane(onClear: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.no_results_heading),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(Res.string.no_results_body),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(onClick = onClear) {
            Text(stringResource(Res.string.clear_search))
        }
    }
}

@Composable
private fun ErrorBanner(
    error: NoteLibraryError,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = errorMessage(error),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(Res.string.retry))
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    note: NoteItemUi,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_confirm_title)) },
        text = {
            Text(
                stringResource(
                    Res.string.delete_confirm_body,
                    note.title.ifBlank { stringResource(Res.string.untitled_note) },
                ),
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.confirm_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
private fun searchSummary(state: NoteLibraryUiState): String = if (state.query.isBlank()) {
    pluralStringResource(
        Res.plurals.notes_visible,
        state.visibleNotes.size,
        state.visibleNotes.size,
    )
} else {
    pluralStringResource(
        Res.plurals.notes_matching,
        state.visibleNotes.size,
        state.visibleNotes.size,
        state.query,
    )
}

@Composable
private fun errorMessage(error: NoteLibraryError): String = when (error) {
    NoteLibraryError.LoadFailed -> stringResource(Res.string.error_load)
    NoteLibraryError.SaveFailed -> stringResource(Res.string.error_save)
    NoteLibraryError.DeleteFailed -> stringResource(Res.string.error_delete)
}
