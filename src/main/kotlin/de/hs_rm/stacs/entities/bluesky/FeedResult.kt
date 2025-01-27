package de.hs_rm.stacs.entities.bluesky

/**
 * DataClass for BlueSky Response
 */
data class FeedResult (
    val feed: Array<FeedDTO>,
    val cursor: String
)