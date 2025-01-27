package de.hs_rm.stacs.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PostRepository : ReactiveCrudRepository<Post, String> {

    /**
     * Needed since our posts use the uri as the id.
     * The normal save() method does not work since it tries to do an UPDATE
     */
    @Query("INSERT INTO posts (uri, author, content, lang, timestamp, sentimentscore) VALUES (:uri, :author, :content, :lang, :timestamp, :sentimentscore)")
    fun insertPost(uri: String, author: String, content: String, lang: String?, timestamp: String, sentimentscore: Double): Mono<Post>

    /**
     * Gets every post containing the searchWord inside its main text (content)
     */
    fun findByContentContainingIgnoreCase(content: String): Flux<Post>

    /**
     * Fetches a limited number of posts sorted by their timestamps
     */
    @Query("SELECT * FROM posts ORDER BY timestamp::timestamp DESC LIMIT :limit")
    fun getLimitedSortedByTime(limit: Int): Flux<Post>

    /**
     * Fetches all Posts containing the searchWord.
     * Optionally filters by date and lang
     */
    @Query("SELECT * FROM posts WHERE POSITION(LOWER(:word) in LOWER(content)) > 0 AND (:timestamp IS NULL OR timestamp < :timestamp) AND (:lang IS NULL OR lang = :lang) ORDER BY timestamp::timestamp DESC LIMIT :limit")
    fun findByContentWithOptionalFilters(word: String, timestamp: String?, lang: String?, limit: Int): Flux<Post>

    /**
     * Fetches all Posts with the corresponding author
     */
    fun findByAuthor(author: String): Flux<Post>
}