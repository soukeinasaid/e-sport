package utilies;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class ImageGeneratorAPI {
    
    // Using Hugging Face free inference API (no API key required for free tier)
    private static final String API_URL = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0";
    
    /**
     * Generate an avatar image using the API
     * @param prompt Description for the avatar
     * @return Base64 encoded image string, or null if failed
     */
    public static String generateAvatar(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Create JSON request body
            String jsonInputString = String.format(
                "{\"inputs\":\"%s\",\"parameters\":{\"num_inference_steps\":20}}",
                prompt.replace("\"", "\\\"")
            );
            
            System.out.println("Sending request to Hugging Face API");
            
            // Send request
            try(java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read binary image data
                try(InputStream is = connection.getInputStream()) {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    byte[] imageBytes = baos.toByteArray();
                    System.out.println("Received " + imageBytes.length + " bytes");
                    return Base64.getEncoder().encodeToString(imageBytes);
                }
            } else {
                // Read error
                try(java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getErrorStream()))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                    System.err.println("API Error: " + error.toString());
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("Error generating avatar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate a default avatar for a user based on their name
     * @param firstName User's first name
     * @param lastName User's last name
     * @return Base64 encoded image string, or null if failed
     */
    public static String generateDefaultAvatar(String firstName, String lastName) {
        String prompt = String.format("A cute cartoon avatar of a person named %s %s, simple minimalist style, white background, friendly expression, anime style, high quality", 
                                     firstName, lastName);
        return generateAvatar(prompt);
    }
}
