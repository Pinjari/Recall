package com.recallflow.embeddings

/** Platform implementations report availability before a caller requests an embedding. */
interface EmbeddingProvider {
    val capability: EmbeddingCapability

    /** Returns a vector in the available model's embedding space. */
    suspend fun embed(text: String): List<Float>
}

sealed interface EmbeddingCapability {
    data object Unavailable : EmbeddingCapability

    data class Available(
        val modelId: String,
        val dimensions: Int,
    ) : EmbeddingCapability
}
