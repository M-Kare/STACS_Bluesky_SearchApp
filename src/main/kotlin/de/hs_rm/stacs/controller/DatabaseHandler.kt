package de.hs_rm.stacs.controller

import de.hs_rm.stacs.database.Post
import de.hs_rm.stacs.service.PostService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * Handles all request that are purely database related
 */
@Component
class DatabaseHandler(private val postService: PostService) {
    val LIMIT = 300
    val logger = LoggerFactory.getLogger(DatabaseHandler::class.java)

    /**
     * Fetches all Posts from the database
     */
    fun getPosts(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(postService.getAllPosts(), Post::class.java)
    }

    /**
     * Fetches a limited ammount of posts from the database
     */
    fun getLimitedPosts(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(postService.getLimitedPosts(LIMIT), Post::class.java)
    }
}