package de.hs_rm.stacs.database

import de.hs_rm.stacs.entities.bluesky.PostDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("posts")
data class Post(
    @Id
    val uri: String,
    val author: String,
    val content: String,
    val lang: String?,
    val timestamp: String,
    val sentimentscore: Double,
){
    /**
     * Function to create posts from PostDTOs.
     * Sentiment need to be given aswell, since it is determined after the PostDTO is created.
     * PostDTOs lang can be null and is fetched from BlueSky as an Array. We only save the first value to simplify things
     */
    companion object{
        fun create(postDTO: PostDTO, sentiment: Double) = Post(
            uri = postDTO.uri,
            author = postDTO.author.handle,
            content = postDTO.record.text.replace(Regex("^\\s*$"), ""),
            lang = if(postDTO.record.langs?.isNotEmpty() == true) postDTO.record.langs[0] else null,
            timestamp = postDTO.record.createdAt,
            sentimentscore = sentiment
        )
    }
}