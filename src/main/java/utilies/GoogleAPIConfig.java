package utilies;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAPIConfig {
    private static final String JSON_FILE_PATH = "c:\\Users\\saids\\Downloads\\esport-495222-02e575c68448.json";
    private static GoogleCredentials credentials;
    private static final String CLIENT_ID = "112902474613316517759";
    
    static {
        try {
            initializeCredentials();
        } catch (Exception e) {
            System.err.println("Failed to initialize Google API credentials: " + e.getMessage());
        }
    }
    
    private static void initializeCredentials() throws IOException, GeneralSecurityException {
        try (FileInputStream serviceAccount = new FileInputStream(JSON_FILE_PATH)) {
            credentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/userinfo.email"));
        }
    }
    
    public static GoogleCredentials getCredentials() {
        return credentials;
    }
    
    public static String getClientId() {
        return CLIENT_ID;
    }
    
    public static GoogleIdTokenVerifier createTokenVerifier() {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        
        return new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .setIssuer("https://accounts.google.com")
                .build();
    }
    
    public static boolean isConfigured() {
        return credentials != null;
    }
    
    public static String getClientEmail() {
        if (credentials != null) {
            try {
                // Get the service account email from the credentials
                return credentials.createScopedRequired() ? "soukaina@esport-495222.iam.gserviceaccount.com" : null;
            } catch (Exception e) {
                System.err.println("Error getting client email: " + e.getMessage());
            }
        }
        return "soukaina@esport-495222.iam.gserviceaccount.com";
    }
}
