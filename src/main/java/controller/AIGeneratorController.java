package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import utilies.AIContentGenerator;
import utilies.HuggingFaceAI;
import utilies.Session;

import java.util.ArrayList;
import java.util.List;

public class AIGeneratorController {

    @FXML
    private TextField titleField;

    
    @FXML
    private TextField topicField;

    @FXML
    private Button generateBtn;

    @FXML
    private Button copyBtn;

    @FXML
    private Button useBtn;

    @FXML
    private Button backBtn;

    @FXML
    private VBox generatedContentContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        System.out.println("AIGeneratorController: Initialized");
        updateStatusLabel("Ready to generate AI content!");
    }

    @FXML
    public void handleGenerate() {
        System.out.println("AIGeneratorController: Generate button clicked");
        try {
            String title = titleField.getText().trim();
            String topic = topicField.getText().trim();

            if (title.isEmpty() && topic.isEmpty()) {
                updateStatusLabel("Please enter a title or topic to generate content");
                return;
            }

            // Generate content based on title or topic using HuggingFace AI
            String generatedContent;
            String generatedTitle;

            if (!title.isEmpty()) {
                generatedContent = HuggingFaceAI.generateContent(title);
                generatedTitle = title;
            } else {
                generatedTitle = HuggingFaceAI.generateTitle(topic);
                generatedContent = HuggingFaceAI.generateContent(topic);
                titleField.setText(generatedTitle);
            }

            // Display generated content
            displayGeneratedContent(generatedTitle, generatedContent);
            updateStatusLabel("Content generated successfully!");

        } catch (Exception e) {
            System.err.println("Error generating content: " + e.getMessage());
            updateStatusLabel("Error generating content. Please try again.");
        }
    }

    @FXML
    public void handleCopy() {
        try {
            // Get the generated content from the display
            String content = getGeneratedContent();
            if (content != null && !content.trim().isEmpty()) {
                // Copy to clipboard using simple string approach
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(content);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                updateStatusLabel("Content copied to clipboard!");
            } else {
                updateStatusLabel("No content to copy!");
            }
        } catch (Exception e) {
            updateStatusLabel("Failed to copy to clipboard");
        }
    }

    @FXML
    public void handleUse() {
        String title = titleField.getText().trim();
        String content = getGeneratedContent();

        if (title.isEmpty() || content == null || content.trim().isEmpty()) {
            updateStatusLabel("Please generate content first!");
            return;
        }

        // Navigate back to forum with generated content
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/forum.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Get the forum controller and set the generated content
            controller.ForumController forumController = loader.getController();
            if (forumController != null) {
                forumController.setTitleField(title);
                forumController.setContentArea(content);
            }
            
            // Get the current stage and set new scene
            javafx.stage.Stage stage = (javafx.stage.Stage) useBtn.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            stage.setScene(scene);
            stage.setTitle("VictoryGrid - Forum");
            stage.show();

            updateStatusLabel("Content transferred to forum post form!");

        } catch (Exception e) {
            System.err.println("Error navigating to forum: " + e.getMessage());
            updateStatusLabel("Error navigating to forum. Please try again.");
        }
    }

    @FXML
    public void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/forum.fxml"));
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = (javafx.stage.Stage) backBtn.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            stage.setScene(scene);
            stage.setTitle("VictoryGrid - Forum");
            stage.show();

        } catch (Exception e) {
            System.err.println("Error going back to forum: " + e.getMessage());
        }
    }

    private void displayGeneratedContent(String title, String content) {
        // Set the title field
        titleField.setText(title);
        
        // Clear previous content
        generatedContentContainer.getChildren().clear();
        
        // Create content display card
        VBox contentCard = new VBox(10);
        contentCard.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 8px; -fx-padding: 15px; -fx-background-radius: 8px;");
        contentCard.setMaxWidth(Double.MAX_VALUE);
        
        Label titleLabel = new Label("Generated Title:");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #495057;");
        
        Label titleValue = new Label(title);
        titleValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #212529; -fx-font-weight: bold; -fx-wrap-text: true;");
        titleValue.setMaxWidth(Double.MAX_VALUE);
        
        Label contentLabel = new Label("Generated Content:");
        contentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #495057; -fx-padding-top: 10px;");
        
        Label contentValue = new Label(content);
        contentValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #212529; -fx-wrap-text: true; -fx-line-spacing: 2px;");
        contentValue.setMaxWidth(Double.MAX_VALUE);
        
        contentCard.getChildren().addAll(titleLabel, titleValue, contentLabel, contentValue);
        generatedContentContainer.getChildren().add(contentCard);
    }
    
    private String getGeneratedContent() {
        // Get the content from the displayed card
        if (generatedContentContainer.getChildren().isEmpty()) {
            return "";
        }
        
        VBox contentCard = (VBox) generatedContentContainer.getChildren().get(0);
        if (contentCard.getChildren().size() >= 4) {
            Label contentValue = (Label) contentCard.getChildren().get(3);
            return contentValue.getText();
        }
        
        return "";
    }

    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
    }
}
