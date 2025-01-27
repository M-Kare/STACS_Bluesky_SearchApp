package de.hs_rm.stacs.entities.bluesky

/**
 * DataClass for BlueSky Response
 */
data class PostDTO(
    val uri: String,
    val author: Author,
    val record: Content,
)
