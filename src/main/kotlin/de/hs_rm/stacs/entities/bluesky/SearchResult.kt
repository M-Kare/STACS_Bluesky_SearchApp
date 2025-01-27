package de.hs_rm.stacs.entities.bluesky

/**
 * DataClass for BlueSky Response
 */
data class SearchResult(
    val posts: Array<PostDTO>,
    val cursor: Int
)