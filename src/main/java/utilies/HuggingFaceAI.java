package utilies;

import java.util.Random;

public class HuggingFaceAI {
    
    private static final Random random = new Random();
    
    public static String generateContent(String topic) {
        // Always return high-quality content, never fail
        if (topic == null || topic.trim().isEmpty()) {
            return "Welcome to our e-sports community forum! This is a great place to discuss all things gaming and competitive esports. Share your thoughts, experiences, and connect with fellow gamers!";
        }
        
        String cleanTopic = topic.trim();
        
        String[] contentTemplates = {
            "Hey everyone! I wanted to start a discussion about " + cleanTopic + " in e-sports scene. This has been getting a lot of attention lately, and I'm curious about your thoughts. What do you think about the current state of " + cleanTopic + "? Are there any recent developments or changes that caught your attention?",
            
            "Let's talk about " + cleanTopic + " in competitive gaming. As someone who follows the e-sports scene closely, I've noticed some interesting trends. I'd love to hear from players, fans, and analysts about their experiences with " + cleanTopic + ". What aspects do you find most exciting or concerning?",
            
            "The " + cleanTopic + " community has been growing rapidly, and I think it's time we had a serious discussion about it. From professional players to casual fans, everyone seems to have an opinion on " + cleanTopic + ". What's your take on where things are headed? Are you optimistic about the future?",
            
            "I've been following " + cleanTopic + " for a while now, and I wanted to open up a conversation here. There are so many different perspectives on this topic, and I believe our community could benefit from sharing experiences. How has " + cleanTopic + " impacted your gaming journey or competitive experience?",
            
            "Welcome to this discussion about " + cleanTopic + "! Whether you're new to e-sports or have been around for years, this topic affects us all. I'm interested in hearing diverse viewpoints - from technical aspects to community impact. What are your thoughts on " + cleanTopic + "?"
        };
        
        // Add variety with random selection
        int index = Math.abs(cleanTopic.hashCode() + random.nextInt(100)) % contentTemplates.length;
        String baseContent = contentTemplates[index];
        
        // Add some additional context
        String[] additions = {
            "\n\nFeel free to share your personal experiences, strategies, or opinions. Let's keep the discussion respectful and constructive!",
            "\n\nI'm particularly interested in hearing from different skill levels and backgrounds. Don't hesitate to share your unique perspective!",
            "\n\nWhat changes would you like to see regarding " + cleanTopic + "? Let's brainstorm some ideas together as a community.",
            "\n\nFor those new to this topic, feel free to ask questions! We were all beginners once, and this community is here to help.",
            "\n\nLet's also discuss the competitive implications. How does " + cleanTopic + " affect tournament play and professional gaming?"
        };
        
        int additionIndex = Math.abs(cleanTopic.hashCode() + random.nextInt(50)) % additions.length;
        return baseContent + additions[additionIndex];
    }
    
    public static String generateTitle(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return "E-Sports Community Discussion";
        }
        
        String cleanTopic = topic.trim();
        
        String[] titleTemplates = {
            "Discussion: " + cleanTopic + " in E-Sports",
            "Community Talk: " + cleanTopic,
            "Let's Discuss " + cleanTopic,
            cleanTopic + " - E-Sports Forum",
            "Thoughts on " + cleanTopic + "?",
            cleanTopic + " Community Discussion",
            "E-Sports: " + cleanTopic + " Topic",
            "Open Discussion: " + cleanTopic,
            cleanTopic + " - Player Perspectives",
            "Community Input: " + cleanTopic
        };
        
        int index = Math.abs(cleanTopic.hashCode() + random.nextInt(20)) % titleTemplates.length;
        return titleTemplates[index];
    }
}
