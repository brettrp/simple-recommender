package powley.recommender

import com.opencsv.CSVIterator
import com.opencsv.CSVReader
import groovy.util.logging.Slf4j
import org.apache.mahout.cf.taste.common.NoSuchItemException
import org.apache.mahout.cf.taste.common.NoSuchUserException
import org.apache.mahout.cf.taste.common.TasteException
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender
import org.apache.mahout.cf.taste.similarity.ItemSimilarity
import org.apache.mahout.cf.taste.similarity.UserSimilarity

import java.util.concurrent.ConcurrentHashMap

/**
 *  SimpleRecommender
 *
 *  This is a singleton class which provides a UserRecommender and an ItemRecommender for a dataset in two files
 *
 *    recommendation file:  standard recommendation file with userid, itemid, rating
 *    item file: item details, containing at least one column with the itemid
 *
 *    Note that the userid and itemid must be numeric, and that the recommendation file can't have a heading
 *    (comment it out with # if you like)
 *
 *    The item database is kept in memory; for greater scalability this could be written to a database or search engine.
 *    Recommendations are not precomputed or cached, so the response time for an individual recommendation may be slow.
 *
 */

@Slf4j
class SimpleRecommender {

    /**  In memory database of item details, keyed by item id. */
    Map<Long,Map> itemDetails = new ConcurrentHashMap<Long,Map>()

    /** The Mahout data model. */
    FileDataModel           dataModel

    /** Mahout user based recommender. */
    UserBasedRecommender    userRecommender

    /** Mahout item based recommender. */
    ItemBasedRecommender    itemRecommender

    /** Singleton instance of us. */
    static SimpleRecommender instance

    static Object              lock = new Object()

    /**
     * Get the singleton instance of us.
     * @returns the singleton SimpleRecommender
     */
    static SimpleRecommender getInstance() {
        if (instance == null) {
            synchronized(lock) {
                if (instance == null)
                    instance = new SimpleRecommender()
            }
        }
        instance
    }

    /**
     * Read item records from the given file, and the specified key field, and store in our in-memory database.
     * The key must be numeric.
     * @param path path to the file to read
     * @param keyField the name of the keyfield, from the header of the file
     */
    void readItemRecords(String path, String keyField) {
        log.info "Loading items from ${path}..."

        CSVReader reader = new CSVReader(new FileReader(path))
        def headers = []

        for (String[] line in new CSVIterator(new CSVReader(new FileReader(path)))) {
            if (!headers) {
                // If we're on the first line, save the headers
                headers = line
            } else {
                // Create map of header:value and store it in our in memory database keyed by the key field (item id)
                def record = [:]
                headers.eachWithIndex { header, index -> record[header] = line[index] }
                Long itemId = Long.parseLong(record[keyField])
                itemDetails[itemId] = record
            }

        }

        log.info "Loaded ${itemDetails.size()} items from ${path}"

    }

    /**
     * Build user and item based recommenders from the recommendations in the given standard format file
     * with format userid, itemid, rating
     * @param path path to the file
     */

    void buildRecommenders(String path) {
        log.info "Building user recommender from ${path}..."

        // Create the data model from the given file
        dataModel = new FileDataModel(new File(path))

        // Create the user recommender
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel)
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel)
        userRecommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity)

        // Create the item recommender
        ItemSimilarity itemSimilarity = new EuclideanDistanceSimilarity(dataModel)
        itemRecommender = new GenericItemBasedRecommender(dataModel, itemSimilarity)

    }


    /**
     * Get a list of user based recommendations
     * @param userId the user id
     * @param count the number of recommendations to return
     * @return a list of maps of user details, or null if the user doesn't exist
     */
    List getUserRecommendation(long userId, int count) {
        log.debug "getUserRecommendation ${userId} ${count}"

        try {
            List recommendations = userRecommender.recommend( userId, count)
            recommendations.collect{ itemDetails[it.itemID] }
        } catch (NoSuchUserException ne) {
            // In future, we could add a temporary user with preferences to get recommendations for a new user
            log.debug "User ${userId} doesn't exist"
            return null
        }
    }

    /**
     * Get a list of item based recommendations
     * @param itemId the item id
     * @param count the number of recommendations to return
     * @return a list of maps of user details
     */

    List getItemRecommendation(long itemId, int count) {
        try {
            List recommendations = itemRecommender.recommend(itemId, count)
            recommendations.collect { itemDetails[it.itemID] }
        } catch (TasteException te) {
            log.debug "Item ${itemId} doesn't exist"
            return null
        }
    }

}
