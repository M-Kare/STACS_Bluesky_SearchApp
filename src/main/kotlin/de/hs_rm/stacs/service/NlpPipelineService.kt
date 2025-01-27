package de.hs_rm.stacs.service

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import java.util.Properties
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import edu.stanford.nlp.trees.Tree
import org.springframework.stereotype.Service

/**
 * This Service uses the Stanford CoreNLP to analyse text.
 */
@Service
class NlpPipelineService{
    private lateinit var pipeline: StanfordCoreNLP

    /**
     * Sets the needed properties for the StanfordCoreNLP.
     * The specified properties ensure we get a sentiment analysis.
     */
    init {
        val props: Properties = Properties();
        props.setProperty("annotators", "tokenize, pos, parse, sentiment")
        pipeline = StanfordCoreNLP(props)
    }

    /**
     *  Analyses the given text sentence for sentence.
     *  Returns the average sentiment score for the text.
     */
    fun estimateSentiment(text: String): Double{
        var score = 0.0
        var counter = 0
        val annotation = pipeline.process(text);
        for(sentence in annotation.get(CoreAnnotations.SentencesAnnotation::class.java)){
           val tree: Tree = sentence.get(SentimentAnnotatedTree::class.java);
            val sentimentInt: Int = RNNCoreAnnotations.getPredictedClass(tree);
            score += sentimentInt;
            counter ++;
        }
        if(counter == 0){return -1.0}
        score /= counter
        return score;
    }
}
