package de.hs_rm.stacs.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfig {

    @Bean
    fun bsRouter(bsHandler: BlueSkyHandler, dbHandler: DatabaseHandler, gmHandler: GeminiHandler) = router{
        "api".nest{
            GET("/posts/limited", dbHandler::getLimitedPosts)
            GET("/posts", dbHandler::getPosts)
            GET("/posts/own", bsHandler::myPosts)
            GET("/search", bsHandler::searchPosts)
            GET("/search/expanded", bsHandler::expandedSearchPosts)
            "/user".nest{
                POST("/post", bsHandler::postPost)
            }
            "/gemini".nest{
                POST("/summarize", gmHandler::postSummarizePrompt)
                POST("/findSimilarPosts", gmHandler::postFindSimilarPosts)
            }
        }
    }
}