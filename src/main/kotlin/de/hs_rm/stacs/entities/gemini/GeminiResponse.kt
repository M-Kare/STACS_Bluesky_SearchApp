package de.hs_rm.stacs.entities.gemini

/**
 * DataClass for Gemini Response
 */
data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata,
    val modelVersion: String
)
