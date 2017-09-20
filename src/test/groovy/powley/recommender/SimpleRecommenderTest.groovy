package powley.recommender

import groovyx.net.http.RESTClient
import org.apache.mahout.cf.taste.eval.RecommenderBuilder
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator
import org.eclipse.jetty.server.Server
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.Test
import org.junit.runners.MethodSorters


/**
 * Tests for SimpleRecommender and the web service
 *
 * Tests are run in order so that we can read the files and build the models once.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SimpleRecommenderTest
{

    @Test
    void test1BuildRecommenders() {
        SimpleRecommender recommender = SimpleRecommender.getInstance()
        recommender.readItemRecords("src/main/datasets/anime/anime.csv", "anime_id")
        assert recommender.itemDetails.size() > 0

        recommender.buildRecommenders("src/main/datasets/anime/rating.csv" )
        assert recommender.userRecommender != null
        assert recommender.itemRecommender != null

    }

    @Test
    void test2GetRecommendations() {

        SimpleRecommender recommender = SimpleRecommender.getInstance()

        long userId = 833
        println "Getting user recommendations for ${userId}"
        def userRecommendations = recommender.getUserRecommendation(userId, 3)
        println userRecommendations
        assert userRecommendations.size() == 3

        long itemId = 214
        println "Getting item recommendations for ${itemId}"
        def itemRecommendations = recommender.getItemRecommendation(itemId, 3)
        println itemRecommendations
        assert itemRecommendations.size() == 3

    }

    @Test
    void test3WebService() {
        Server server = Application.createServer(8888)
        server.start()


        def client = new RESTClient( 'http://localhost:8888/' )
        def response = client.get( path: 'recommender/user/833', query:['count':3] )

        assert response.status == 200
        assert response.data.size() == 3
        println response.data

        response = client.get( path : 'recommender/item/214', query:['count':4] )
        assert response.status == 200
        assert response.data.size() == 4
        println response.data

        server.stop()
        server.destroy()
    }

    @Ignore
    void test4RecommenderEvaluator() {

        // Not strictly a test, but print out some evaluation statistics for our recommender
        // Note that this takes a long time to run on our full dataset
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

        RecommenderBuilder builder = new SimpleRecommenderBuilder();
        double result = evaluator.evaluate(builder, null, recommender.dataModel, 0.9, 1.0);
        println "AverageAbsoluteDifferenceRecommenderEvaluator: ${result}";

    }

}
