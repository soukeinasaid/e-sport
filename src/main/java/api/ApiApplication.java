package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded HTTP Server for REST APIs
 * Non-blocking API server based on Java's built-in HttpServer
 */
public class ApiApplication {

    private static final Logger logger = Logger.getLogger(ApiApplication.class.getName());
    private static HttpServer server;

    /**
     * Start the embedded HTTP server
     */
    public static void startServer() {
        if (server != null) {
            logger.info("Server is already running");
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 9090), 0);

            // Register API handlers
            server.createContext("/api/tournois", new TournoiApiHandler());
            server.createContext("/api/centres", new CentreApiHandler());
            server.createContext("/health", exchange -> {
                String response = "{\"status\": \"UP\"}";
                sendResponse(exchange, 200, response);
            });

            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
            server.start();

            logger.info("REST API server started on http://localhost:9090");
            logger.info("Available endpoints:");
            logger.info("  - GET    /api/tournois");
            logger.info("  - GET    /api/tournois/{id}");
            logger.info("  - POST   /api/tournois");
            logger.info("  - PUT    /api/tournois/{id}");
            logger.info("  - DELETE /api/tournois/{id}");
            logger.info("  - GET    /api/centres");
            logger.info("  - GET    /api/centres/{id}");
            logger.info("  - POST   /api/centres");
            logger.info("  - PUT    /api/centres/{id}");
            logger.info("  - DELETE /api/centres/{id}");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start HTTP server: " + e.getMessage(), e);
        }
    }

    /**
     * Stop the HTTP server
     */
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            logger.info("REST API server stopped");
        }
    }

    /**
     * Send HTTP response
     */
    protected static void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        exchange.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
    }

    /**
     * Create error response
     */
    protected static String createErrorResponse(String message) {
        return new JSONObject()
                .put("success", false)
                .put("error", message)
                .toString();
    }

    /**
     * Create success response
     */
    protected static String createSuccessResponse(String message, Object data) {
        return new JSONObject()
                .put("success", true)
                .put("message", message)
                .put("data", data)
                .toString();
    }
}


