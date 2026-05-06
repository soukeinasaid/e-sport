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

public class ForumController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private VBox postsContainer;

    @FXML
    private Button publishBtn;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchBtn;

    @FXML
    private Button recentBtn;

    @FXML
    private Button oldestBtn;

    @FXML
    private Button allBtn;

    @FXML
    private Button favoritesBtn;

    @FXML
    private HBox paginationContainer;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private Label totalPostsLabel;

    @FXML
    private TextField pageInputField;

    @FXML
    private Button goToPageBtn;

    private ForumService forumService = new ForumService();
    private FavoriteService favoriteService = new FavoriteService();

    // 🔥 stocker le post en cours de modification
    private Forum selectedForum = null;

    // Filter state
    private String currentSearch = "";
    private String currentFilter = "all"; // recent, oldest, all

    // Pagination state
    private int currentPage = 1;
    private int postsPerPage = 5;
    private int totalPages = 1;
    private List<Forum> allFilteredPosts = new ArrayList<>();

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadPosts();
        
        // Add Enter key support for search
        searchField.setOnAction(e -> handleSearch());
        
        // Add Enter key support for page input
        pageInputField.setOnAction(e -> handleGoToPage());
    }

    // ================= ADD / UPDATE =================
    @FXML
    public void handlePublish() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Missing fields", "Please fill in both title and content");
            return;
        }

        if (selectedForum == null) {
            // CREATE
            Forum newForum = new Forum(title, content, Session.getUserId());
            forumService.addForum(newForum);
        } else {
            // UPDATE
            selectedForum.setTitre(title);
            selectedForum.setDescription(content);
            forumService.updateForum(selectedForum);
            selectedForum = null;
            publishBtn.setText("Publish");
        }

        // Clear fields
        titleField.clear();
        contentArea.clear();

        // Refresh
        loadPosts();
    }

    @FXML
    public void handleAdd() {
        handlePublish(); // Delegate to handlePublish for consistency
    }

    // ================= LOAD POSTS =================
    private void loadPosts() {
        loadPostsWithFilters();
    }

    // ================= SEARCH AND FILTER =================
    @FXML
    public void handleSearch() {
        currentSearch = searchField.getText().trim();
        loadPostsWithFilters();
    }

    @FXML
    public void handleRecent() {
        System.out.println("Recent button clicked");
        currentFilter = "recent";
        updateFilterButtons(recentBtn);
        loadPostsWithFilters();
    }

    @FXML
    public void handleOldest() {
        System.out.println("Oldest button clicked");
        currentFilter = "oldest";
        updateFilterButtons(oldestBtn);
        loadPostsWithFilters();
    }

    @FXML
    public void handleAll() {
        System.out.println("All button clicked");
        currentFilter = "all";
        updateFilterButtons(allBtn);
        loadPostsWithFilters();
    }

    @FXML
    public void handleFavorites() {
        System.out.println("Favorites button clicked - navigating to favorites interface");
        try {
            // Load the favorites view
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/favorites.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Get the current stage and set new scene
            javafx.stage.Stage stage = (javafx.stage.Stage) favoritesBtn.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            // Apply CSS
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("My Favorites");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading favorites view: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not open favorites page. Please try again.");
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

    private void loadPostsWithFilters() {
        // Reset to first page when filters change
        currentPage = 1;
        System.out.println("Loading posts with filter: " + currentFilter);
        
        List<Forum> allPosts = forumService.getAll();
        allFilteredPosts = new ArrayList<>();

        // SEARCH FILTER
        for (Forum post : allPosts) {
            if (currentSearch.isEmpty() || 
                post.getTitre().toLowerCase().contains(currentSearch.toLowerCase())) {
                allFilteredPosts.add(post);
            }
        }

        // SORT FILTER
        switch (currentFilter) {
            case "recent":
                System.out.println("Sorting by recent (newest first)");
                allFilteredPosts.sort(Comparator.comparing(Forum::getDateCreation).reversed());
                break;
            case "oldest":
                System.out.println("Sorting by oldest (oldest first)");
                allFilteredPosts.sort(Comparator.comparing(Forum::getDateCreation));
                break;
            case "all":
                System.out.println("Keeping original order (all)");
                // Keep original order from database
                break;
        }

        // Calculate total pages
        totalPages = (int) Math.ceil((double) allFilteredPosts.size() / postsPerPage);
        if (totalPages == 0) totalPages = 1;

        // Display current page
        displayCurrentPage();
        updatePaginationButtons();
        updatePageInformation();

        // SHOW RESULTS COUNT
        if (!currentSearch.isEmpty() || !"all".equals(currentFilter)) {
            showResultsCount(allFilteredPosts.size());
        }
    }

    private void updateFilterButtons(Button activeBtn) {
        System.out.println("Updating filter buttons, active: " + activeBtn.getText());
        // Reset all buttons
        recentBtn.getStyleClass().remove("active");
        oldestBtn.getStyleClass().remove("active");
        allBtn.getStyleClass().remove("active");
        favoritesBtn.getStyleClass().remove("active");
        
        // Set active button
        activeBtn.getStyleClass().add("active");
        System.out.println("Active button set to: " + activeBtn.getText());
    }

    private void showResultsCount(int count) {
        String message = currentSearch.isEmpty() ? 
            "Showing " + count + " posts" : 
            "Found " + count + " posts for \"" + currentSearch + "\"";
        
        // You could update a label here if you add one to the FXML
        System.out.println(message);
    }

    // ================= PAGINATION METHODS =================
    private void displayCurrentPage() {
        postsContainer.getChildren().clear();
        
        int startIndex = (currentPage - 1) * postsPerPage;
        int endIndex = Math.min(startIndex + postsPerPage, allFilteredPosts.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            postsContainer.getChildren().add(createPostCard(allFilteredPosts.get(i)));
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
        System.out.println("Updating page information - Current page: " + currentPage + ", Total pages: " + totalPages + ", Total posts: " + allFilteredPosts.size());
        
        // Update page info label
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        
        // Update total posts label
        int startIndex = (currentPage - 1) * postsPerPage + 1;
        int endIndex = Math.min(currentPage * postsPerPage, allFilteredPosts.size());
        
        if (allFilteredPosts.isEmpty()) {
            totalPostsLabel.setText("No posts found - Create your first post!");
        } else {
            totalPostsLabel.setText("Showing " + startIndex + "-" + endIndex + " of " + allFilteredPosts.size() + " posts");
        }
        
        System.out.println("Page info updated: " + pageInfoLabel.getText() + " | " + totalPostsLabel.getText());
    }

    // ================= CARD UI =================
    private VBox createPostCard(Forum f) {

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

        // ❤️ FAVORITE HEART ICON
        int currentUserId = Session.getUserId();
        boolean isFavorite = favoriteService.isFavorite(f.getIdForum(), currentUserId);
        
        Button favoriteBtn = new Button(isFavorite ? "❤️" : "🤍");
        favoriteBtn.getStyleClass().add("favorite-btn");
        
        favoriteBtn.setOnAction(e -> {
            if (favoriteService.isFavorite(f.getIdForum(), currentUserId)) {
                favoriteService.removeFavorite(f.getIdForum(), currentUserId);
                favoriteBtn.setText("🤍");
                System.out.println("Removed from favorites: " + f.getTitre());
            } else {
                favoriteService.addFavorite(f.getIdForum(), currentUserId);
                favoriteBtn.setText("❤️");
                System.out.println("Added to favorites: " + f.getTitre());
            }
        });

        // 🔐 OWNER CHECK
        if (f.getIdUser() == Session.getUserId()) {

            Button editBtn = new Button("✏");
            editBtn.getStyleClass().add("icon-btn");

            editBtn.setOnAction(e -> {
                selectedForum = f;

                titleField.setText(f.getTitre());
                contentArea.setText(f.getDescription());

                publishBtn.setText("Update");
            });

            Button deleteBtn = new Button("🗑");
            deleteBtn.getStyleClass().add("icon-btn-delete");

            deleteBtn.setOnAction(e -> {
                forumService.deleteForum(f.getIdForum());
                loadPosts();
            });

            actions.getChildren().addAll(favoriteBtn, editBtn, deleteBtn);
        } else {
            // Non-owners can only favorite
            actions.getChildren().add(favoriteBtn);
        }

        header.getChildren().addAll(title, spacer, actions);

        card.getChildren().addAll(header, desc);

        return card;
    }

    // ================= UTIL =================
    private void clearFields() {
        titleField.clear();
        contentArea.clear();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}
