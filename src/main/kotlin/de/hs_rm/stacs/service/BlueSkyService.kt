package de.hs_rm.stacs.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.hs_rm.stacs.entities.bluesky.FeedResult
import de.hs_rm.stacs.entities.bluesky.SearchResult
import de.hs_rm.stacs.entities.bluesky.SessionResponse
import de.hs_rm.stacs.entities.bluesky.MyFeed
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * This Service handles all interactions with the BlueSky API
 */
@Service
class BlueSkyService(private val postService: PostService) {
    val logger = LoggerFactory.getLogger(BlueSkyService::class.java)
    val whatsHotFeedUri = "at://did:plc:z72i7hdynmk6r22z27h6tvur/app.bsky.feed.generator/whats-hot"
    val verifiedNewsUri = "at://did:plc:kkf4naxqmweop7dv4l2iqqf5/app.bsky.feed.generator/verified-news"
    val webClient: WebClient = WebClient.create()

    /**
     * Makes a GET-Request to the BlueSky public Endpoint searchPosts. Uses sort=top to get more relevant posts.
     * Has a backup method that does an authenticated request if the normal request fails.
     * (The Backup was needed since the normal Endpoint stopped working for a while on BlueSkys side.)
     */
    fun fetchPosts(searchTerm: String, limit: Int, until: String?, lang: String?):Mono<SearchResult>{
        logger.info("Fetching $limit posts for $searchTerm, until $until, language: $lang")
        val response: Mono<SearchResult> = webClient.get()
            .uri("https://public.api.bsky.app/xrpc/app.bsky.feed.searchPosts?q=$searchTerm&limit=$limit&sort=top${if(until == null) "" else "&until=${until}"}${if(lang == null) "" else "&lang=$lang"}")
            .retrieve()
            .bodyToMono(SearchResult::class.java)
            .onErrorResume { e -> logger.error("Normal Search failed, doing authenticated search ({})",e.message)
                fetchPostsWithAuth(searchTerm, limit, until, lang) }
        response.map{ println(it.posts.size) }.subscribe()
        return response
    }

    /**
     * Overloaded Method which does not need until and lang
     */
    fun fetchPosts(searchTerm: String, limit: Int): Mono<SearchResult> {
        return fetchPosts(searchTerm, limit, "", "")
    }

    /**
     * Backup searchPosts Function. Does the searchPosts request to a specific host, so we can use our account to authenticate.
     * The normal searchPosts Endpoint does not allow authentication.
     */
    fun fetchPostsWithAuth(searchTerm: String, limit: Int, until: String?, lang: String?):Mono<SearchResult>{
        return getToken().flatMap { sessionResponse ->
        webClient.get()
            .uri("https://polypore.us-west.host.bsky.network/xrpc/app.bsky.feed.searchPosts?q=$searchTerm&limit=$limit&sort=top${if(until == null) "" else "&until=${until}"}${if(lang == null) "" else "&lang=$lang"}")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${sessionResponse.accessJwt}")
            .retrieve()
            .bodyToMono(SearchResult::class.java)
        }
    }

    /**
     * Gets a limited number of posts from the given feed.
     * Uses the public BlueSky getFeed Endpoint
     */
    fun fetchPostsFromFeed(feedUri: String, limit: Int):Mono<FeedResult>{
        val response = webClient.get()
            .uri("https://public.api.bsky.app/xrpc/app.bsky.feed.getFeed?feed=$feedUri&limit=$limit")
            .retrieve()
            .bodyToMono(FeedResult::class.java)
        return response
    }

    /**
     *  Creates an authenticated Session with BlueSky and uses that session to post the given text to the BlueSky account.
     *  The account need to be set via environment variables BSNAME (if not set uses stackingstacs) and BSPASS.
     *  Uses the createRecord BlueSky API Endpoint
     */
    fun postNewPost(text: String): Mono<Int>{
        return getToken()
            .flatMap { sessionResponse ->
                val objectMapper = ObjectMapper()
                val bodyContent = mapOf(
                    "repo" to sessionResponse.did,
                    "collection" to "app.bsky.feed.post",
                    "record" to mapOf(
                        "\$type" to "app.bsky.feed.post",
                        "text" to text,
                        "createdAt" to getCurrentTimeForPost()
                    )
                )
                val bodyContentJson = objectMapper.writeValueAsString(bodyContent)

                return@flatMap webClient.post()
                    .uri("https://bsky.social/xrpc/com.atproto.repo.createRecord")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${sessionResponse.accessJwt}")
                    .body(BodyInserters.fromValue(bodyContentJson))
                    .retrieve()
                    .onStatus({ status -> status.isError }) { response ->
                        logger.error("Error while posting new post: {}", response.statusCode())
                        throw RuntimeException("Error occurred during post ${response.statusCode()}")
                    }
                    .toEntity(String::class.java)
                    .map { it.statusCode.value() }
            }
    }

    /**
     * Fetches all Posts from the account set in BSNAME.
     * Uses the BlueSky public Endpoint getAuthorFeed
     */
    fun fetchMyPosts(): Mono<MyFeed> {
        val myAt = if(System.getenv()["BSNAME"] != null) System.getenv()["BSNAME"] else "stackingstacs.bsky.social"
        val response = webClient.get()
            .uri("https://public.api.bsky.app/xrpc/app.bsky.feed.getAuthorFeed?actor=$myAt")
            .retrieve()
            .bodyToMono(MyFeed::class.java)
        return response
    }

    /**
     * Creates an authenticated Session with BlueSky using the set account.
     * This returns a JWT-Token which is used to authenticate actions that require authentication
     */
    fun getToken(): Mono<SessionResponse> {
        val webClient = WebClient.create("https://bsky.social")

        val requestBody = mapOf(
            "identifier" to if(System.getenv()["BSNAME"] != null) System.getenv()["BSNAME"] else "stackingstacs.bsky.social",
            "password" to System.getenv()["BSPASS"]
        )
        logger.info("Used Account: {}",requestBody["identifier"].toString())

        return webClient.post()
            .uri("/xrpc/com.atproto.server.createSession")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(SessionResponse::class.java)
            .onErrorMap { e -> Exception("Failed to get accessJWT", e)}
    }

    /**
     * Returns the current datetime in the specified format
     */
    fun getCurrentTimeForPost(): String {
        val currentTime = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return currentTime.format(formatter)
    }

    /**
     * This method is executed every minute.
     * It regularly fetches new posts from two specified feeds and saves them in our database.
     * This is to ensure our database gets filled with posts automatically.
     */
    @Scheduled(fixedDelay = 60000)
    fun fetchNewPostsLoop(){
        logger.info("Fetching from {} and {}", whatsHotFeedUri, verifiedNewsUri)
        fetchPostsFromFeed(whatsHotFeedUri, 50)
            .flatMapMany { data -> Flux.fromArray(data.feed) }
            .flatMap { feedDTO ->
                postService.createPost(feedDTO.post)
            }.subscribe()
        fetchPostsFromFeed(verifiedNewsUri, 50)
            .flatMapMany { data -> Flux.fromArray(data.feed) }
            .flatMap { feedDTO ->
                postService.createPost(feedDTO.post)
            }.subscribe()
    }

}
