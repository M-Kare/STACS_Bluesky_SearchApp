package de.hs_rm.stacs.entities.bluesky

/**
 * DataClass for BlueSky Response
 */
data class Content(
    val createdAt: String,
    val langs: Array<String>?,
    val text: String
)
