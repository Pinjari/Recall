package com.recallflow.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class NoteSearchTest {
    @Test
    fun exactPhraseRanksAbovePartialAndTokenMatches() {
        val notes = listOf(
            note("token", title = "Project notes", body = "Recall the launch plan."),
            note("partial", title = "Projector notes"),
            note("phrase", title = "Project Recall"),
        )

        assertEquals(
            listOf("phrase", "token", "partial"),
            searchNotes(notes, "project recall").map(Note::id),
        )
    }

    @Test
    fun partialAndTokenMatchesAreCaseInsensitive() {
        val notes = listOf(
            note("title-partial", title = "Apples for launch"),
            note("body-token", body = "The apple is ready."),
        )

        assertEquals(
            listOf("title-partial", "body-token"),
            searchNotes(notes, "APP").map(Note::id),
        )
    }

    @Test
    fun titleMatchRanksAboveBodyOnlyMatch() {
        val notes = listOf(
            note("body", title = "Daily notes", body = "Remember Apollo."),
            note("title", title = "Apollo mission"),
        )

        assertEquals(
            listOf("title", "body"),
            searchNotes(notes, "apollo").map(Note::id),
        )
    }

    @Test
    fun emptyQueryPreservesRecentNoteInputOrder() {
        val recentOrder = listOf(
            note("newest", updatedAt = 3L),
            note("middle", updatedAt = 2L),
            note("oldest", updatedAt = 1L),
        )

        assertEquals(recentOrder, searchNotes(recentOrder, ""))
    }

    @Test
    fun noResultsReturnsAnEmptyList() {
        assertEquals(emptyList(), searchNotes(listOf(note("one"), note("two")), "missing"))
    }

    @Test
    fun equalScoresKeepTheirInputOrder() {
        val inputOrder = listOf(
            note("first", title = "Alpha"),
            note("second", title = "Alpha"),
        )

        assertEquals(inputOrder.map(Note::id), searchNotes(inputOrder, "alpha").map(Note::id))
    }

    private fun note(
        id: String,
        title: String = "Offline first",
        body: String = "The local database is the source of truth.",
        updatedAt: Long = 1L,
    ) = Note(
        id = id,
        title = title,
        body = body,
        createdAt = 1L,
        updatedAt = updatedAt,
    )
}
