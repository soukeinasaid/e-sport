package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import entity.Forum;
import service.ForumService;
import service.FavoriteService;
import utilies.Session;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class FavoritesController {

    @FXML
    private Button backBtn;

    @FXML
    private VBox favoritesContainer;

    @FXML
    private VBox emptyStateContainer;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private Label totalFavoritesLabel;

    @FXML
    private TextField pageInputField;

    @FXML
    private Button goToPageBtn;

    @FXML
    private HBox paginationContainer;

    @FXML
    private Button browseForumBtn;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private Button publishBtn;

    private ForumService forumService = new ForumService();
    private FavoriteService favoriteService = new FavoriteService();

    // 🔥 stocker le post en cours de modification
    private Forum selectedForum = null;

    // Pagination state
    private int currentPage = 1;
    private int postsPerPage = 5;
    private int totalPages = 1;
    private List<Forum> allFavorites = new ArrayList<>();

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadFavorites();
        
        // Add Enter key support for page input
        pageInputField.setOnAction(e -> handleGoToPage());
    }

    // ================= NAVIGATION =================
    @FXML
    public void handleBackToForum() {
        try {
            // Load the forum view
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/forum.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Get the current stage and set new scene
            javafx.stage.Stage stage = (javafx.stage.Stage) backBtn.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            // Apply CSS
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Forum");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading forum view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleGoToPage() {
        try {
            String input = pageInputField.getText().trim();
            if (input.isEmpty()) {
                return;
            }
            
            int pageNumber = Integer.parseInt(input);
            if (pageNumber < 1 || pageNumber > totalPages) {
                showAlert("Invalid Page", "Please enter a page number between 1 and " + totalPages);
                pageInputField.clear();
                return;
            }
            
            System.out.println("Direct navigation to page: " + pageNumber);
            goToPage(pageNumber);
            pageInputField.clear();
            
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number");
            pageInputField.clear();
        }
    }

    // ================= CRUD METHODS =================
    @FXML
    public void handlePublish() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Missing fields", "Please fill in both title and content");
            return;
        }

        if (selectedForum == null) {
            // ADD NEW POST
            Forum newForum = new Forum(title, content, Session.getUserId());
            forumService.addForum(newForum);
            System.out.println("New post added and favorites refreshed");
        } else {
            // UPDATE EXISTING POST
            selectedForum.setTitre(title);
            selectedForum.setDescription(content);
            forumService.updateForum(selectedForum);
            System.out.println("Post updated and favorites refreshed");
        }
        
        // Clear fields and reset
        titleField.clear();
        contentArea.clear();
        publishBtn.setText("Publish");
        selectedForum = null;
        
        // Refresh favorites list
        loadFavorites();
    }

    @FXML
    public void handleDelete(int forumId) {
        forumService.deleteForum(forumId);
        loadFavorites();
        System.out.println("Post deleted and favorites refreshed");
    }

    // ================= LOAD FAVORITES =================
    private void loadFavorites() {
        int userId = Session.getUserId();
        System.out.println("Loading favorites for user: " + userId);
        
        allFavorites = forumService.getFavoritesForUser(userId);
        System.out.println("Retrieved " + allFavorites.size() + " favorites from database");
        
        // Debug: Print each favorite
        for (Forum forum : allFavorites) {
            System.out.println("Favorite: " + forum.getTitre() + " (ID: " + forum.getIdForum() + ")");
        }
        
        // Calculate total pages
        totalPages = (int) Math.ceil((double) allFavorites.size() / postsPerPage);
        if (totalPages == 0) totalPages = 1;
        System.out.println("Total pages: " + totalPages);

        // Display current page
        displayCurrentPage();
        updatePaginationButtons();
        updatePageInformation();
        updateEmptyState();
        
        System.out.println("Favorites loading completed");
    }

    // ================= PAGINATION METHODS =================
    private void displayCurrentPage() {
        favoritesContainer.getChildren().clear();
        
        int startIndex = (currentPage - 1) * postsPerPage;
        int endIndex = Math.min(startIndex + postsPerPage, allFavorites.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            favoritesContainer.getChildren().add(createFavoriteCard(allFavorites.get(i)));
        }
    }
    
    private void updatePaginationButtons() {
        paginationContainer.getChildren().clear();
        
        System.out.println("Updating pagination buttons. Total pages: " + totalPages + ", Current page: " + currentPage);
        
        if (totalPages <= 1) {
            // Always show pagination for testing, even with 1 page
            Button singlePageBtn = new Button("1");
            singlePageBtn.getStyleClass().add("pagination-number");
            singlePageBtn.getStyleClass().add("active");
            singlePageBtn.setDisable(true);
            paginationContainer.getChildren().add(singlePageBtn);
            System.out.println("Single page pagination created");
            return;
        }
        
        // Previous button
        Button prevBtn = new Button("←");
        prevBtn.getStyleClass().add("pagination-nav");
        prevBtn.setDisable(currentPage == 1);
        prevBtn.setOnAction(e -> goToPage(currentPage - 1));
        paginationContainer.getChildren().add(prevBtn);
        
        // Page number buttons
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);
        
        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }
        
        // First page button if not in range
        if (startPage > 1) {
            Button firstBtn = new Button("1");
            firstBtn.getStyleClass().add("pagination-number");
            firstBtn.setOnAction(e -> goToPage(1));
            paginationContainer.getChildren().add(firstBtn);
            
            if (startPage > 2) {
                Label dots1 = new Label("...");
                dots1.getStyleClass().add("pagination-dots");
                paginationContainer.getChildren().add(dots1);
            }
        }
        
        // Number buttons
        for (int i = startPage; i <= endPage; i++) {
            final int pageNumber = i; // Make effectively final
            Button pageBtn = new Button(String.valueOf(pageNumber));
            pageBtn.getStyleClass().add("pagination-number");
            if (pageNumber == currentPage) {
                pageBtn.getStyleClass().add("active");
            }
            pageBtn.setOnAction(e -> goToPage(pageNumber));
            paginationContainer.getChildren().add(pageBtn);
        }
        
        // Last page button if not in range
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                Label dots2 = new Label("...");
                dots2.getStyleClass().add("pagination-dots");
                paginationContainer.getChildren().add(dots2);
            }
            
            Button lastBtn = new Button(String.valueOf(totalPages));
            lastBtn.getStyleClass().add("pagination-number");
            lastBtn.setOnAction(e -> goToPage(totalPages));
            paginationContainer.getChildren().add(lastBtn);
        }
        
        // Next button
        Button nextBtn = new Button("→");
        nextBtn.getStyleClass().add("pagination-nav");
        nextBtn.setDisable(currentPage == totalPages);
        nextBtn.setOnAction(e -> goToPage(currentPage + 1));
        paginationContainer.getChildren().add(nextBtn);
    }
    
    private void goToPage(int pageNumber) {
        if (pageNumber < 1 || pageNumber > totalPages) {
            return;
        }
        currentPage = pageNumber;
        displayCurrentPage();
        updatePaginationButtons();
        updatePageInformation();
    }

    private void updatePageInformation() {
        System.out.println("Updating page information - Current page: " + currentPage + ", Total pages: " + totalPages + ", Total favorites: " + allFavorites.size());
        
        // Update page info label
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        
        // Update total favorites label
        int startIndex = (currentPage - 1) * postsPerPage + 1;
        int endIndex = Math.min(currentPage * postsPerPage, allFavorites.size());
        
        if (allFavorites.isEmpty()) {
            totalFavoritesLabel.setText("No favorites found");
        } else {
            totalFavoritesLabel.setText("Showing " + startIndex + "-" + endIndex + " of " + allFavorites.size() + " favorites");
        }
        
        System.out.println("Page info updated: " + pageInfoLabel.getText() + " | " + totalFavoritesLabel.getText());
    }

    private void updateEmptyState() {
        if (allFavorites.isEmpty()) {
            favoritesContainer.setVisible(false);
            favoritesContainer.setManaged(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            paginationContainer.setVisible(false);
            paginationContainer.setManaged(false);
        } else {
            favoritesContainer.setVisible(true);
            favoritesContainer.setManaged(true);
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
            paginationContainer.setVisible(true);
            paginationContainer.setManaged(true);
        }
    }

    // ================= CARD UI =================
    private VBox createFavoriteCard(Forum f) {
        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(f.getTitre());
        title.getStyleClass().add("post-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);

        Label desc = new Label(f.getDescription());
        desc.getStyleClass().add("post-description");
        desc.setWrapText(true);
        desc.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8);

        // ❤️ REMOVE FROM FAVORITES
        int currentUserId = Session.getUserId();
        
        Button removeFavoriteBtn = new Button("💔");
        removeFavoriteBtn.getStyleClass().add("icon-btn-delete");
        
        removeFavoriteBtn.setOnAction(e -> {
            favoriteService.removeFavorite(f.getIdForum(), currentUserId);
            loadFavorites(); // Refresh the favorites list
            System.out.println("Removed from favorites: " + f.getTitre());
        });

        // 🔐 OWNER CHECK - can edit and delete own posts even from favorites
        if (f.getIdUser() == Session.getUserId()) {

            Button editBtn = new Button("✏");
            editBtn.getStyleClass().add("icon-btn");

            editBtn.setOnAction(e -> {
                selectedForum = f;
                titleField.setText(f.getTitre());
                contentArea.setText(f.getDescription());
                publishBtn.setText("Update");
                System.out.println("Editing post: " + f.getTitre());
            });

            Button deleteBtn = new Button("🗑");
            deleteBtn.getStyleClass().add("icon-btn-delete");

            deleteBtn.setOnAction(e -> {
                handleDelete(f.getIdForum());
                System.out.println("Deleted post: " + f.getTitre());
            });

            actions.getChildren().addAll(removeFavoriteBtn, editBtn, deleteBtn);
        } else {
            // Non-owners can only remove from favorites
            actions.getChildren().add(removeFavoriteBtn);
        }

        header.getChildren().addAll(title, spacer, actions);

        card.getChildren().addAll(header, desc);

        return card;
    }

    // ================= UTIL =================
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}
