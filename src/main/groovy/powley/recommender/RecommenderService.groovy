package powley.recommender


import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

import javax.ws.rs.DefaultValue
import javax.ws.rs.GET;
import javax.ws.rs.Path
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


/**
 *  Simple web service for recommender.  Provides two methods:
 *      /recommender/user/n     - provide user based recommendation for user n
 *      /recommender/item/n     - provide item based recommendation for item n
 *  Both methods have an optional ?count= parameter to specify the maximum number of recommendations to return.
 */

@Slf4j
@Path("/recommender")
class RecommenderService  {

    /**
     * Return a list of user based recommendations as JSON
     * @param id the id of the user
     * @param count the maximum number of recommendations to return
     * @return the response including the JSON list of recommendations
     */
    @GET
    @Path("user/{id}")
    @Produces( MediaType.APPLICATION_JSON )
    public Response recommendUser(@PathParam("id") String id, @DefaultValue("4") @QueryParam("count") int count) {
        log.debug "recommend for user ${id} count: ${count}"

        def recommendations = SimpleRecommender.instance.getUserRecommendation(Long.parseLong(id), count)
        if (recommendations != null) {
            String json = JsonOutput.toJson(recommendations)
            log.debug "recommendations: ${json}"
            Response.status(Response.Status.OK).entity(json).build()
        } else {
            log.debug "User ${id} does not exist"
            Response.status(Response.Status.NOT_FOUND).entity('{"error":"User ' + id + ' does not exist"}').build()
        }

    }

    /**
     * Return a list of item based recommendations as JSON
     * @param id the id of the item
     * @param count the maximum number of recommendations to return
     * @return the response including the JSON list of recommendations
     */
    @GET
    @Path("item/{id}")
    @Produces( MediaType.APPLICATION_JSON )
    public Response recommendItem(@PathParam("id") String id, @DefaultValue("4") @QueryParam("count") int count) {
        log.debug "recommend for item ${id}"

        def recommendations = SimpleRecommender.instance.getItemRecommendation(Long.parseLong(id), count)
        if (recommendations != null) {
            String json = JsonOutput.toJson(recommendations)
            log.debug "recommendations: ${json}"
            Response.status(Response.Status.OK).entity(json).build();
        } else {
            log.debug "Item ${id} does not exist"
            Response.status(Response.Status.NOT_FOUND).entity('{"error":"Item ' + id + ' does not exist"}').build()
        }
    }

}

