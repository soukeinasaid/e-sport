package utilies;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class GoogleOAuth2Helper {
    private static final String CLIENT_ID = "112902474613316517759";
    private static boolean initialized = true;

    public static String getAuthorizationUrl() {
        // Use a simple Google OAuth2 URL that doesn't require complex setup
        return "https://accounts.google.com/o/oauth2/auth?" +
               "client_id=" + CLIENT_ID +
               "&redirect_uri=http://localhost:8080" +
               "&scope=https://www.googleapis.com/auth/userinfo.email" +
               "&response_type=code" +
               "&access_type=offline";
    }

    public static void openGoogleSignInPage() {
        try {
            String authUrl = getAuthorizationUrl();
            
            // Try to open in default browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(authUrl));
                System.out.println("Opened Google sign-in page in browser");
                System.out.println("If the page doesn't work, please copy this URL:");
                System.out.println(authUrl);
            } else {
                // Fallback: print URL to console
                System.out.println("Please open this URL in your browser:");
                System.out.println(authUrl);
            }
        } catch (Exception e) {
            System.err.println("Failed to open Google sign-in page: " + e.getMessage());
            // Fallback: print URL to console
            String authUrl = getAuthorizationUrl();
            System.out.println("Please open this URL in your browser:");
            System.out.println(authUrl);
        }
    }

    public static boolean isConfigured() {
        return initialized;
    }
}
