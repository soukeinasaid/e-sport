package service;

import entity.Favorite;
import utilies.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteService {

    private Connection cnx;

    public FavoriteService() {
        cnx = DatabaseConfig.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("FavoriteService: Database connection is null. Cannot perform operations.");
        }
    }

    // ADD TO FAVORITES
    public void addFavorite(int idForum, int idUser) {
        System.out.println("FavoriteService: Adding favorite - Forum " + idForum + " by User " + idUser);
        
        if (cnx == null) {
            System.err.println("FavoriteService: Database connection is null. Cannot perform operations.");
            return;
        }
        
        // Check if already favorited
        if (isFavorite(idForum, idUser)) {
            System.out.println("FavoriteService: Forum already in favorites");
            return;
        }
        
        String sql = "INSERT INTO favorites (idForum, idUser) VALUES (?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idForum);
            ps.setInt(2, idUser);
            int result = ps.executeUpdate();
            System.out.println("FavoriteService: Added to favorites successfully - Forum " + idForum + " by User " + idUser + " (rows affected: " + result + ")");

        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("Table")) {
                System.out.println("FavoriteService: Favorites table doesn't exist, skipping add operation");
            } else {
                System.err.println("FavoriteService: Error adding favorite: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // REMOVE FROM FAVORITES
    public void removeFavorite(int idForum, int idUser) {
        if (cnx == null) {
            System.err.println("FavoriteService: Database connection is null. Cannot perform operations.");
            return;
        }
        
        String sql = "DELETE FROM favorites WHERE idForum = ? AND idUser = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idForum);
            ps.setInt(2, idUser);
            ps.executeUpdate();
            System.out.println("Removed from favorites: Forum " + idForum + " by User " + idUser);

        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("Table")) {
                System.out.println("FavoriteService: Favorites table doesn't exist, skipping remove operation");
            } else {
                System.out.println("Error removing favorite: " + e.getMessage());
            }
        }
    }

    // CHECK IF FAVORITE
    public boolean isFavorite(int idForum, int idUser) {
        System.out.println("FavoriteService: Checking if Forum " + idForum + " is favorited by User " + idUser);
        
        if (cnx == null) {
            System.err.println("FavoriteService: Database connection is null. Cannot perform operations.");
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM favorites WHERE idForum = ? AND idUser = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idForum);
            ps.setInt(2, idUser);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isFav = rs.getInt(1) > 0;
                    System.out.println("FavoriteService: Is favorite? " + isFav + " (count: " + rs.getInt(1) + ")");
                    return isFav;
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("Table")) {
                System.out.println("FavoriteService: Favorites table doesn't exist, returning false");
                return false;
            } else {
                System.err.println("FavoriteService: Error checking favorite: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("FavoriteService: Is favorite? false (no results)");
        return false;
    }

    // GET USER'S FAVORITES
    public List<Integer> getUserFavoriteForumIds(int idUser) {
        List<Integer> favoriteIds = new ArrayList<>();
        
        if (cnx == null) {
            System.err.println("FavoriteService: Database connection is null. Cannot perform operations.");
            return favoriteIds;
        }
        
        String sql = "SELECT idForum FROM favorites WHERE idUser = ? ORDER BY dateAdded DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    favoriteIds.add(rs.getInt("idForum"));
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("Table")) {
                System.out.println("FavoriteService: Favorites table doesn't exist, returning empty list");
            } else {
                System.out.println("Error getting user favorites: " + e.getMessage());
            }
        }

        return favoriteIds;
    }

    // GET ALL FAVORITES (for a user)
    public List<Favorite> getUserFavorites(int idUser) {
        List<Favorite> favorites = new ArrayList<>();
        
        if (cnx == null) {
            System.err.println("FavoriteService: Database connection is null. Cannot perform operations.");
            return favorites;
        }
        
        String sql = "SELECT * FROM favorites WHERE idUser = ? ORDER BY dateAdded DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Favorite fav = new Favorite();
                    fav.setIdFavorite(rs.getInt("idFavorite"));
                    fav.setIdForum(rs.getInt("idForum"));
                    fav.setIdUser(rs.getInt("idUser"));
                    fav.setDateAdded(rs.getTimestamp("dateAdded").toLocalDateTime());
                    favorites.add(fav);
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("Table")) {
                System.out.println("FavoriteService: Favorites table doesn't exist, returning empty list");
            } else {
                System.out.println("Error getting user favorites: " + e.getMessage());
            }
        }

        return favorites;
    }
}
