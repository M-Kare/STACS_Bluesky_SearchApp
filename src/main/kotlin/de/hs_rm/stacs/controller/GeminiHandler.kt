package de.hs_rm.stacs.controller

import de.hs_rm.stacs.entities.gemini.GeminiResponse
import de.hs_rm.stacs.entities.gemini.Part
import de.hs_rm.stacs.service.GeminiService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * Handler for Gemini related requests
 */
@Component
class GeminiHandler(private val geminiService: GeminiService) {
    val logger = LoggerFactory.getLogger(GeminiHandler::class.java)

    val summarizePrompt = "Please summarize the given posts in only a few sentences: "
    val findSimilarWordsPrompt = "Based on the given keyword, suggest alternative keywords to expand social media searches and retrieve more relevant posts. Your answer should only contain 3 similar words (excluding the keyword itself) and nothing else. Always format the answer like this: word1, word2, word3. Keyword: "

    /**
     * Receives text through the body (in our case multiple posts) and sends a request to Gemini with the prompt to summarize all the posts.
     */
    fun postSummarizePrompt(response: ServerRequest): Mono<ServerResponse> {
        return response.bodyToMono(String::class.java)
            .flatMap { prompt ->  ServerResponse.ok().body(geminiService.fetchGeminiResponse(prompt, summarizePrompt), Part::class.java)}
    }

    /**
     * Receives text through the body (in our case the searchWord) and request Gemini to give us mutliple related words.
     */
    fun postFindSimilarPosts(response: ServerRequest): Mono<ServerResponse> {
        return response.bodyToMono(String::class.java)
            .flatMap { prompt ->  ServerResponse.ok().body(geminiService.fetchGeminiResponse(prompt, findSimilarWordsPrompt), Part::class.java)}
    }
}