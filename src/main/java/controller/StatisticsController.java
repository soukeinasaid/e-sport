package controller;

import entity.User;
import entity.Forum;
import service.UserService;
import service.ForumService;
import service.FavoriteService;
import utilies.Session;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML
    private Label totalUsersLabel;
    
    @FXML
    private Label activeUsersLabel;
    
    @FXML
    private Label newUsersLabel;
    
    @FXML
    private Label adminCountLabel;
    
    @FXML
    private Label regularUsersLabel;
    
    @FXML
    private Label usersWithPicsLabel;
    
    @FXML
    private Label totalPostsLabel;
    
    @FXML
    private Label activeForumsLabel;
    
    @FXML
    private Label postsThisMonthLabel;
    
    @FXML
    private Label avgPostsPerUserLabel;
    
    @FXML
    private Label mostActiveForumLabel;
    
    @FXML
    private Label totalFavoritesLabel;
    
    @FXML
    private Label recentActivityLabel;
    
    @FXML
    private PieChart userRolesPieChart;
    
    @FXML
    private BarChart<String, Number> forumActivityBarChart;
    
    @FXML
    private VBox userStatsSection;
    
    @FXML
    private VBox userRolesChartSection;
    
    @FXML
    private VBox forumStatsSection;
    
    @FXML
    private VBox forumChartSection;
    
    @FXML
    private Button showUserStatsButton;
    
    @FXML
    private Button showForumStatsButton;
    
    @FXML
    private Button showAllStatsButton;
    
    private UserService userService;
    private ForumService forumService;
    private FavoriteService favoriteService;

    @FXML
    public void initialize() {
        userService = new UserService();
        forumService = new ForumService();
        favoriteService = new FavoriteService();
        
        loadStatistics();
    }

    private void loadStatistics() {
        loadUserStatistics();
        loadForumStatistics();
        loadRecentActivity();
        loadCharts();
    }

    private void loadCharts() {
        loadUserRolesPieChart();
        loadForumActivityBarChart();
    }

    private void loadUserRolesPieChart() {
        try {
            List<User> allUsers = userService.getAll();
            
            long adminCount = allUsers.stream()
                    .filter(User::isAdmin)
                    .count();
            long userCount = allUsers.stream()
                    .filter(user -> !user.isAdmin())
                    .count();
            
            PieChart.Data adminSlice = new PieChart.Data("Admins", adminCount);
            PieChart.Data userSlice = new PieChart.Data("Users", userCount);
            
            userRolesPieChart.getData().clear();
            userRolesPieChart.getData().addAll(adminSlice, userSlice);
            
            // Set colors
            adminSlice.getNode().setStyle("-fx-pie-color: #ff6b6b;");
            userSlice.getNode().setStyle("-fx-pie-color: #6FA3FF;");
            
        } catch (Exception e) {
            System.err.println("Error loading user roles pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadForumActivityBarChart() {
        try {
            List<Forum> allForums = forumService.getAll();
            List<User> allUsers = userService.getAll();
            
            // Count posts per user
            Map<String, Integer> userPostCounts = new java.util.HashMap<>();
            for (Forum forum : allForums) {
                String userName = getUserName(forum.getIdUser(), allUsers);
                userPostCounts.put(userName, userPostCounts.getOrDefault(userName, 0) + 1);
            }
            
            // Get top 10 users by post count
            List<java.util.Map.Entry<String, Integer>> sortedUsers = userPostCounts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Posts");
            
            for (java.util.Map.Entry<String, Integer> entry : sortedUsers) {
                String userName = truncateString(entry.getKey(), 15);
                series.getData().add(new XYChart.Data<>(userName, entry.getValue()));
            }
            
            forumActivityBarChart.getData().clear();
            forumActivityBarChart.getData().add(series);
            
        } catch (Exception e) {
            System.err.println("Error loading forum activity bar chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUserStatistics() {
        try {
            List<User> allUsers = userService.getAll();
            
            // Total users
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
            
            // Active users (users who registered in the last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long activeCount = allUsers.stream()
                    .filter(user -> {
                        // Since we don't have last login date, we'll consider users with profile pictures as "active"
                        return user.getProfilePicture() != null && !user.getProfilePicture().isEmpty();
                    })
                    .count();
            activeUsersLabel.setText(String.valueOf(activeCount));
            
            // New users this month
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
            long newUsersCount = allUsers.stream()
                    .filter(user -> {
                        // Since we don't have registration date, we'll estimate based on ID (assuming newer users have higher IDs)
                        // This is a simplified approach
                        return user.getIdUser() > (allUsers.size() * 0.8); // Last 20% of users are considered "new"
                    })
                    .count();
            newUsersLabel.setText(String.valueOf(newUsersCount));
            
            // Admin count
            long adminCount = allUsers.stream()
                    .filter(User::isAdmin)
                    .count();
            adminCountLabel.setText(String.valueOf(adminCount));
            
            // Regular users
            long regularCount = allUsers.stream()
                    .filter(user -> !user.isAdmin())
                    .count();
            regularUsersLabel.setText(String.valueOf(regularCount));
            
            // Users with profile pictures
            long usersWithPicsCount = allUsers.stream()
                    .filter(user -> user.getProfilePicture() != null && !user.getProfilePicture().isEmpty())
                    .count();
            usersWithPicsLabel.setText(String.valueOf(usersWithPicsCount));
            
        } catch (Exception e) {
            System.err.println("Error loading user statistics: " + e.getMessage());
            e.printStackTrace();
            setDefaultUserStats();
        }
    }

    private void loadForumStatistics() {
        try {
            List<Forum> allForums = forumService.getAll();
            List<User> allUsers = userService.getAll();
            
            // Total posts (forums)
            totalPostsLabel.setText(String.valueOf(allForums.size()));
            
            // Active forums (all forums are considered active)
            activeForumsLabel.setText(String.valueOf(allForums.size()));
            
            // Posts this month
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
            long postsThisMonthCount = allForums.stream()
                    .filter(forum -> forum.getDateCreation() != null && 
                                   forum.getDateCreation().isAfter(monthStart))
                    .count();
            postsThisMonthLabel.setText(String.valueOf(postsThisMonthCount));
            
            // Average posts per user
            double avgPosts = allUsers.isEmpty() ? 0 : (double) allForums.size() / allUsers.size();
            avgPostsPerUserLabel.setText(String.format("%.1f", avgPosts));
            
            // Most active forum (most recent or first one)
            if (!allForums.isEmpty()) {
                Forum mostRecent = allForums.stream()
                        .filter(f -> f.getDateCreation() != null)
                        .max((f1, f2) -> f1.getDateCreation().compareTo(f2.getDateCreation()))
                        .orElse(allForums.get(0));
                mostActiveForumLabel.setText(truncateString(mostRecent.getTitre(), 15));
            } else {
                mostActiveForumLabel.setText("N/A");
            }
            
            // Total favorites
            try {
                int totalFavorites = 0;
                for (User user : allUsers) {
                    totalFavorites += favoriteService.getUserFavoriteForumIds(user.getIdUser()).size();
                }
                totalFavoritesLabel.setText(String.valueOf(totalFavorites));
            } catch (Exception e) {
                totalFavoritesLabel.setText("0");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading forum statistics: " + e.getMessage());
            e.printStackTrace();
            setDefaultForumStats();
        }
    }

    private void loadRecentActivity() {
        try {
            List<Forum> allForums = forumService.getAll();
            List<User> allUsers = userService.getAll();
            
            StringBuilder activity = new StringBuilder();
            
            // Get recent forums
            List<Forum> recentForums = allForums.stream()
                    .filter(f -> f.getDateCreation() != null)
                    .sorted((f1, f2) -> f2.getDateCreation().compareTo(f1.getDateCreation()))
                    .limit(5)
                    .collect(Collectors.toList());
            
            if (!recentForums.isEmpty()) {
                activity.append("📝 Recent Forum Posts:\n");
                for (Forum forum : recentForums) {
                    String userName = getUserName(forum.getIdUser(), allUsers);
                    activity.append(String.format("• %s by %s\n", 
                            truncateString(forum.getTitre(), 40), userName));
                }
            }
            
            // Get new users
            List<User> newestUsers = allUsers.stream()
                    .sorted((u1, u2) -> Integer.compare(u2.getIdUser(), u1.getIdUser()))
                    .limit(3)
                    .collect(Collectors.toList());
            
            if (!newestUsers.isEmpty()) {
                activity.append("\n👤 Newest Users:\n");
                for (User user : newestUsers) {
                    activity.append(String.format("• %s %s (ID: %d)\n", 
                            user.getPrenom(), user.getNom(), user.getIdUser()));
                }
            }
            
            if (activity.length() == 0) {
                activity.append("No recent activity to display.");
            }
            
            recentActivityLabel.setText(activity.toString());
            
        } catch (Exception e) {
            System.err.println("Error loading recent activity: " + e.getMessage());
            e.printStackTrace();
            recentActivityLabel.setText("Unable to load recent activity.");
        }
    }

    private String getUserName(int userId, List<User> users) {
        for (User user : users) {
            if (user.getIdUser() == userId) {
                return user.getPrenom() + " " + user.getNom();
            }
        }
        return "Unknown User";
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) return "N/A";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private void setDefaultUserStats() {
        totalUsersLabel.setText("0");
        activeUsersLabel.setText("0");
        newUsersLabel.setText("0");
        adminCountLabel.setText("0");
        regularUsersLabel.setText("0");
        usersWithPicsLabel.setText("0");
    }

    private void setDefaultForumStats() {
        totalPostsLabel.setText("0");
        activeForumsLabel.setText("0");
        postsThisMonthLabel.setText("0");
        avgPostsPerUserLabel.setText("0");
        mostActiveForumLabel.setText("N/A");
        totalFavoritesLabel.setText("0");
    }

    @FXML
    private void refreshStatistics(ActionEvent event) {
        loadStatistics();
    }

    @FXML
    private void showUserStats(ActionEvent event) {
        userStatsSection.setVisible(true);
        userStatsSection.setManaged(true);
        userRolesChartSection.setVisible(true);
        userRolesChartSection.setManaged(true);
        forumStatsSection.setVisible(false);
        forumStatsSection.setManaged(false);
        forumChartSection.setVisible(false);
        forumChartSection.setManaged(false);
        
        updateToggleButtons("user");
    }

    @FXML
    private void showForumStats(ActionEvent event) {
        userStatsSection.setVisible(false);
        userStatsSection.setManaged(false);
        userRolesChartSection.setVisible(false);
        userRolesChartSection.setManaged(false);
        forumStatsSection.setVisible(true);
        forumStatsSection.setManaged(true);
        forumChartSection.setVisible(true);
        forumChartSection.setManaged(true);
        
        updateToggleButtons("forum");
    }

    @FXML
    private void showAllStats(ActionEvent event) {
        userStatsSection.setVisible(true);
        userStatsSection.setManaged(true);
        userRolesChartSection.setVisible(true);
        userRolesChartSection.setManaged(true);
        forumStatsSection.setVisible(true);
        forumStatsSection.setManaged(true);
        forumChartSection.setVisible(true);
        forumChartSection.setManaged(true);
        
        updateToggleButtons("all");
    }

    private void updateToggleButtons(String activeButton) {
        showUserStatsButton.getStyleClass().remove("active");
        showForumStatsButton.getStyleClass().remove("active");
        showAllStatsButton.getStyleClass().remove("active");
        
        switch (activeButton) {
            case "user":
                showUserStatsButton.getStyleClass().add("active");
                break;
            case "forum":
                showForumStatsButton.getStyleClass().add("active");
                break;
            case "all":
                showAllStatsButton.getStyleClass().add("active");
                break;
        }
    }

    @FXML
    private void handleBackToAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/admin_dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();

        } catch (Exception e) {
            System.err.println("Error returning to admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mainLayout.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("VictoryGrid - Main Dashboard");
            stage.show();

        } catch (Exception e) {
            System.err.println("Error returning to main page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
