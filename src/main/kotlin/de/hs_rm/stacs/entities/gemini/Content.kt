package de.hs_rm.stacs.entities.gemini

/**
 * DataClass for Gemini Response
 */
data class Content(
    val parts: List<Part>,
    val role: String
)
