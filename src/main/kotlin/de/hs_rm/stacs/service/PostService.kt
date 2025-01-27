package de.hs_rm.stacs.service

import de.hs_rm.stacs.database.Post
import de.hs_rm.stacs.entities.bluesky.PostDTO
import de.hs_rm.stacs.database.PostRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * This Service houses Database related functions
 */
@Service
class PostService(private val postRepo: PostRepository, private val nlp: NlpPipelineService) {
    val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Creates a Post from a PostDTO and only saves it, if it does not exist in the database
     */
    fun createPost(postDTO: PostDTO): Mono<Post> {
        if(postDTO.record.text.isBlank()){
            logger.info("Post has not text")
            return Mono.empty()
        }

        val post = Post.create(postDTO, nlp.estimateSentiment(postDTO.record.text))

        return postRepo.existsById(post.uri).flatMap { exists ->
            if(!exists){
                postRepo.insertPost(post.uri, post.author, post.content, post.lang, post.timestamp, post.sentimentscore)
            } else {
                Mono.empty()
            }
        }
    }

    /**
     * Fetches all Post from the Database
     */
    fun getAllPosts(): Flux<Post> {
        return postRepo.findAll()
    }

    /**
     * Gets a limited number of posts sorted by the timestamp (newest to oldest)
     */
    fun getLimitedPosts(limit: Int): Flux<Post> {
        return postRepo.getLimitedSortedByTime(limit)
    }

    /**
     * Fetches every post from the database that contains the given keyword
     * (no longer used since findPostsByKeywordDateLang does the same while also being able to filter by lang and date)
     */
    fun findPostsByKeyword(keyword: String): Flux<Post> {
        return postRepo.findByContentContainingIgnoreCase(keyword)
    }

    /**
     * Fetches every post from the database that contains the given keyword.
     * Optionally a date (Format: "yyyy-mm-ddThh:mm:ssZ") can be passed to filter the posts by a older timestamp.
     * Optionally a lang (Format: two letter ISO-country-code) can be passed to only fetch posts with the given lang.
     */
    fun findPostsByKeywordDateLang(word: String, date: String?, lang: String?, limit: Int): Flux<Post> {
        return postRepo.findByContentWithOptionalFilters(word, date, lang, limit)
    }

    /**
     * Fetches all posts from the given author
     */
    fun findMyPosts(author: String): Flux<Post> {
        return postRepo.findByAuthor(author)
    }
}