package de.hs_rm.stacs.entities.gemini

/**
 * DataClass for Gemini Response
 */
data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)
