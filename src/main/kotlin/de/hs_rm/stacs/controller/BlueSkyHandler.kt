package de.hs_rm.stacs.controller

import de.hs_rm.stacs.database.Post
import de.hs_rm.stacs.service.BlueSkyService
import de.hs_rm.stacs.service.GeminiService
import de.hs_rm.stacs.service.NlpPipelineService
import de.hs_rm.stacs.service.PostService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ValidatedLangDelegate
import ValidatedSearchwordDelegate
import ValidatedDateDelegate

/**
 * Responsible for BlueSky related requests
 */
@Component
class BlueSkyHandler(private val bsService: BlueSkyService, private val gmService: GeminiService, private val postService: PostService, private val nlp: NlpPipelineService) {
    val LIMIT = 300
    val findTopic = "Format your answer like this: OriginalWord;word1;word2;word3;word4. Please find 4 other words about the same topic as "
    val logger = LoggerFactory.getLogger(BlueSkyHandler::class.java)

    /**
     * Orients itself on the BlueSky API searchPosts Endpoint.
     * Receives a word from the url as a query parameter.
     * A date (yyyy-mm-ddTHH:MM:SSZ) and lang (two-letter ISO country code) can be set to filter the search.
     * Validates the received variables and uses them to fetch new Posts from BlueSky.
     * These posts are saved in the database to ensure that our database is up to date.
     * Afterward we get all posts matching the search criteria from the database.
     */
    fun searchPosts(request: ServerRequest): Mono<ServerResponse> {
        var searchWord: String by ValidatedSearchwordDelegate("")
        var qUntil: String by ValidatedDateDelegate("")
        var qLang: String by ValidatedLangDelegate("")
        try {
            searchWord = request.queryParam("q").orElse("")
            qUntil = request.queryParam("until").orElse("")
            qLang = request.queryParam("lang").orElse("")
        } catch (e: IllegalArgumentException) {
            return ServerResponse.badRequest().bodyValue("Uncool request bro")
        }
        val until = if (qUntil == "") null else qUntil
        val lang = if (qLang == "") null else qLang
        return bsService.fetchPosts(searchWord, 50, until, lang)
            .flatMapMany { data -> Flux.fromArray(data.posts) }
            .flatMap { postDTO -> postService.createPost(postDTO) }
            .collectList()
            .doOnNext { logger.info("Saved Posts from search: {}", searchWord) }
            .flatMap { ServerResponse.ok().body(postService.findPostsByKeywordDateLang(searchWord, until, lang, LIMIT), Post::class.java) }
    }

    /**
     * Uses the searchWord and a prompt to ask Gemini for related words.
     * These words are used to fetch new posts from BlueSky resulting in a wider search radius.
     * After prompting Gemini, it basically works the same as the normal search but for each Gemini-Word.
     */
    fun expandedSearchPosts(request: ServerRequest): Mono<ServerResponse> {
        var searchWord: String by ValidatedSearchwordDelegate("")
        var qUntil: String by ValidatedDateDelegate("")
        var qLang: String by ValidatedLangDelegate("")
        try {
            searchWord = request.queryParam("q").orElse("")
            qUntil = request.queryParam("until").orElse("")
            qLang = request.queryParam("lang").orElse("")
        } catch (e: IllegalArgumentException) {
            return ServerResponse.badRequest().bodyValue("Uncool request bro")
        }
        val until = if (qUntil == "") null else qUntil
        val lang = if (qLang == "") null else qLang
        return gmService.fetchGeminiResponse(searchWord, findTopic)
            .flatMap { response ->
                Flux.fromIterable(response.text.split(";"))
                    .flatMap{ word ->
                        bsService.fetchPosts(word, 30, until, lang)
                            .flatMapMany { data -> Flux.fromArray(data.posts) }
                            .flatMap{ postDTO -> postService.createPost(postDTO) }
                            .collectList()
                            .thenReturn(word)
                    }.flatMap { word ->
                        logger.info("DOING {}", word)
                        postService.findPostsByKeywordDateLang(word, until, lang, 100)
                    }
                    .distinct{ post -> post.uri}
                    .collectList()
                    }
                    .flatMap { posts->
                        ServerResponse.ok().bodyValue(posts)
            }
    }

    /**
     * Receives text through the body and posts it on BlueSky.
     * The account need to be set via the environment Variables BSNAME (if not set uses stackingstacs account) and BSPASS
     */
    fun postPost(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(String::class.java)
            .flatMap { content ->
                logger.info("Posting $content to BlueSky")
                bsService.postNewPost(content).flatMap { statusCode ->
                ServerResponse.status(statusCode).bodyValue("Post status: $statusCode")
                }
            }
    }

    /**
     * Fetches all Posts from the set account (standard stackingstacs), saves them in the database and returns them
     */
    fun myPosts(request: ServerRequest): Mono<ServerResponse> {
        val myAt = if(System.getenv()["BSNAME"] != null) System.getenv()["BSNAME"] else "stackingstacs.bsky.social"
        return bsService.fetchMyPosts()
            .flatMapMany { data -> Flux.fromArray(data.feed) }
            .flatMap { feedDTO -> postService.createPost(feedDTO.post) }
            .collectList()
            .flatMap { ServerResponse.ok().body(postService.findMyPosts(myAt!!), Post::class.java) }    }
}