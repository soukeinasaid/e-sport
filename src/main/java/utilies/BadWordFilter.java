package utilies;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class BadWordFilter {

    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(
        // Common profanity and offensive words
        "fuck", "shit", "ass", "bitch", "damn", "hell", "crap", "piss",
        "dick", "cock", "pussy", "cunt", "whore", "slut", "bastard",
        "fag", "faggot", "nigger", "nigga", "retard", "retarded",
        "idiot", "stupid", "dumb", "moron", "imbecile",
        // Variations and common misspellings
        "fuk", "fcuk", "sh1t", "b1tch", "d1ck", "p0rn",
        // Hate speech terms
        "kill", "murder", "rape", "suicide", "terrorist",
        // Drug references
        "cocaine", "heroin", "meth", "crack", "weed", "marijuana",
        // Sexual content
        "porn", "sex", "nude", "naked", "xxx"
    ));

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Checks if the given text contains any bad words.
     * @param text The text to check
     * @return true if bad words are found, false otherwise
     */
    public static boolean containsBadWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        
        // Check for exact matches
        for (String badWord : BAD_WORDS) {
            if (lowerText.contains(badWord.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Gets a list of detected bad words in the text.
     * @param text The text to check
     * @return Set of bad words found (empty if none)
     */
    public static Set<String> getDetectedBadWords(String text) {
        Set<String> detected = new HashSet<>();
        
        if (text == null || text.trim().isEmpty()) {
            return detected;
        }

        String lowerText = text.toLowerCase();
        
        for (String badWord : BAD_WORDS) {
            if (lowerText.contains(badWord.toLowerCase())) {
                detected.add(badWord);
            }
        }
        
        return detected;
    }

    /**
     * Censors bad words in the text by replacing them with asterisks.
     * @param text The text to censor
     * @return The censored text
     */
    public static String censorText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String result = text;
        for (String badWord : BAD_WORDS) {
            String regex = "(?i)\\b" + Pattern.quote(badWord) + "\\b";
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < badWord.length(); i++) {
                stars.append("*");
            }
            result = result.replaceAll(regex, stars.toString());
        }
        
        return result;
    }

    /**
     * Checks if the text is appropriate for posting.
     * @param text The text to validate
     * @return true if appropriate, false if contains bad words
     */
    public static boolean isAppropriate(String text) {
        return !containsBadWords(text);
    }
}
