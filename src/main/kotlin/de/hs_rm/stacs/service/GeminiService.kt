package de.hs_rm.stacs.service

import de.hs_rm.stacs.entities.gemini.GeminiResponse
import de.hs_rm.stacs.entities.gemini.Part
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * This Service houses all functions related to Gemini and its API
 */
@Service
class GeminiService {
    val logger = LoggerFactory.getLogger(GeminiService::class.java)
    val webClient: WebClient = WebClient.create()

    /**
     * Makes a request towards the Gemini REST API generateContent.
     * It takes a given prompt and returns Geminis answer.
     * Gemini needs an API-Token to be used. This token is read from the environment variable GOOGLE_API_KEY
     */
    fun fetchGeminiResponse(prompt: String, templatePrompt: String): Mono<Part> {

        val promptWithContent = templatePrompt + prompt

        val bodyContent = """
            {
                "contents":[{
                    "parts":[{
                        "text": "$promptWithContent"
                    }]
                }]
            }
        """.trimIndent()
        return webClient.post()
            .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${System.getenv()["GOOGLE_API_KEY"]}")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(bodyContent))
            .retrieve()
            .bodyToMono(GeminiResponse::class.java)
            .map { it.candidates.first().content.parts.first() }
            .doOnNext{ logger.info("Gemini Response: ${it}") }
    }
}

