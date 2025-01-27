package de.hs_rm.stacs.entities.bluesky

/**
 * DataClass for BlueSky Response
 */
data class SessionResponse(
    val did: String,
    val accessJwt: String,
    val refreshJwt: String,
)
