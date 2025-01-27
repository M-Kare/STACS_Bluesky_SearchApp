package de.hs_rm.stacs

import de.hs_rm.stacs.service.NlpPipelineService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class CoreNLPTests {
    private lateinit var nlpService: NlpPipelineService

    @BeforeEach
    fun setup(){
        nlpService = NlpPipelineService()
    }

    @ParameterizedTest
    @CsvSource(
        "Kai stinks., 0",
        "Today is a good day., 3",
        "I love the world., 3",
        "Ace Attorney is not good., 1",
        "Food is edible., 2",
        "I love how happy and relaxed you are!, 4",
        "Love this energy., 4"
    )
    fun `simple single sentence sentiment tests`(input: String, expected: Double) {
        Assertions.assertEquals(expected, nlpService.estimateSentiment(input))
    }

    @ParameterizedTest
    @CsvSource(
        "Kai stinks. Love this energy!, 2, 2",
        "I love how happy and relaxed you are! Food is edible. Kai stinks. Today is a good day. I love the world. Ace Attorney is not good. Love this energy!, 2, 3",
    )
    fun `long multiple sentence sentiment tests`(input: String, min: Double, max: Double) {
        Assertions.assertTrue(nlpService.estimateSentiment(input) <= max)
        Assertions.assertTrue(nlpService.estimateSentiment(input) >= min)
    }
}