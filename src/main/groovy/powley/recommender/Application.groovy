package powley.recommender


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *  Main application for a simple recommender, serving recommendations via a REST interface
 *  Reads sample data, builds recommenders, and then starts an embedded server to serve
 *  recommendation requests.
 *
 */

public class Application {


    public static void main(String[] args) throws Exception {


        // Initialise recommender
        SimpleRecommender recommender = SimpleRecommender.getInstance()
        recommender.readItemRecords("datasets/anime/anime.csv", "anime_id")
        recommender.buildRecommenders("datasets/anime/rating.csv" )


        // Start up web service
        Server jettyServer = createServer(8080)

        try {
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            System.err.println e.toString()
        } finally {
            jettyServer.destroy();
        }
    }

    /**
     * Create a server with a Jersey servlet handler on the given port
     * @param port the port number to start on
     * @return the Jetty Server
     */
    public static Server createServer( int port ) {
        // Start up web service
        Server jettyServer = new Server(port)

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS)
        context.setContextPath("/")
        jettyServer.setHandler(context)

        ServletHolder jerseyServlet = context.addServlet( org.glassfish.jersey.servlet.ServletContainer.class, "/*")
        jerseyServlet.setInitOrder(1)
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", RecommenderService.class.getCanonicalName())

        jettyServer

    }


}

