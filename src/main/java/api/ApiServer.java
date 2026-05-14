package api;

/**
 * Utility class to start the REST API server
 */
public class ApiServer {

    private static boolean isRunning = false;

    /**
     * Start the REST API server in background thread
     */
    public static void startServer() {
        if (isRunning) {
            return;
        }

        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("[API] Starting REST API server...");
                ApiApplication.startServer();
                isRunning = true;
            } catch (Exception e) {
                System.err.println("[API] Failed: " + e.getMessage());
                isRunning = false;
            }
        }, "ApiServerThread");

        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * Stop the REST API server
     */
    public static void stopServer() {
        if (isRunning) {
            ApiApplication.stopServer();
            isRunning = false;
        }
    }

    public static boolean isServerRunning() {
        return isRunning;
    }
}


