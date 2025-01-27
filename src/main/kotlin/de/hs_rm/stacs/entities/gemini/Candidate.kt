package de.hs_rm.stacs.entities.gemini

/**
 * DataClass for Gemini Response
 */
data class Candidate(
    val content: Content,
    val finishReason: String,
    val avgLogprobs: Double
)
