package com.recallflow.domain

/**
 * Searches notes using case-insensitive lexical matches in their title and body.
 *
 * The ranking tiers are intentionally explicit:
 * 1. an exact query-token phrase in the title,
 * 2. an exact query-token phrase in the body,
 * 3. a partial/token match in the title,
 * 4. a partial/token match in the body.
 *
 * Matching-token coverage breaks ties within a tier. The original input index is
 * the final tie-breaker, so callers can pass the repository's recent-note order
 * and retain it for equal matches. A blank query (or a query without lexical
 * tokens) returns the input list unchanged.
 */
fun searchNotes(notes: List<Note>, query: String): List<Note> {
    val queryTokens = tokenize(query)
    if (queryTokens.isEmpty()) return notes

    return notes
        .asSequence()
        .mapIndexedNotNull { inputIndex, note ->
            score(note, queryTokens)?.let { score ->
                ScoredNote(note = note, score = score, inputIndex = inputIndex)
            }
        }
        .sortedWith(
            compareByDescending<ScoredNote> { it.score.kind.priority }
                .thenByDescending { it.score.field.priority }
                .thenByDescending { it.score.matchedTokenCount }
                .thenBy { it.inputIndex },
        )
        .map { it.note }
        .toList()
}

private data class ScoredNote(
    val note: Note,
    val score: MatchScore,
    val inputIndex: Int,
)

private data class MatchScore(
    val kind: MatchKind,
    val field: MatchField,
    val matchedTokenCount: Int,
)

private enum class MatchKind(val priority: Int) {
    TOKEN(0),
    EXACT_PHRASE(1),
}

private enum class MatchField(val priority: Int) {
    BODY(0),
    TITLE(1),
}

private fun score(note: Note, queryTokens: List<String>): MatchScore? {
    val titleTokens = tokenize(note.title)
    val bodyTokens = tokenize(note.body)
    val titleTokenMatches = matchingTokenCount(titleTokens, queryTokens)
    val bodyTokenMatches = matchingTokenCount(bodyTokens, queryTokens)

    return when {
        containsPhrase(titleTokens, queryTokens) ->
            MatchScore(MatchKind.EXACT_PHRASE, MatchField.TITLE, titleTokenMatches)

        containsPhrase(bodyTokens, queryTokens) ->
            MatchScore(MatchKind.EXACT_PHRASE, MatchField.BODY, bodyTokenMatches)

        titleTokenMatches > 0 ->
            MatchScore(MatchKind.TOKEN, MatchField.TITLE, titleTokenMatches)

        bodyTokenMatches > 0 ->
            MatchScore(MatchKind.TOKEN, MatchField.BODY, bodyTokenMatches)

        else -> null
    }
}

private fun matchingTokenCount(fieldTokens: List<String>, queryTokens: List<String>): Int =
    queryTokens
        .distinct()
        .count { queryToken -> fieldTokens.any { fieldToken -> fieldToken.contains(queryToken) } }

private fun containsPhrase(fieldTokens: List<String>, queryTokens: List<String>): Boolean {
    if (queryTokens.size > fieldTokens.size) return false
    return fieldTokens.windowed(queryTokens.size).any { it == queryTokens }
}

private fun tokenize(value: String): List<String> {
    val normalized = value.lowercase()
    val tokens = mutableListOf<String>()
    var tokenStart = -1

    normalized.forEachIndexed { index, character ->
        if (character.isLetterOrDigit()) {
            if (tokenStart == -1) tokenStart = index
        } else if (tokenStart != -1) {
            tokens += normalized.substring(tokenStart, index)
            tokenStart = -1
        }
    }

    if (tokenStart != -1) tokens += normalized.substring(tokenStart)
    return tokens
}
