package service;

import entity.Forum;
import utilies.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumService {

    private Connection cnx;

    public ForumService() {
        cnx = DatabaseConfig.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("ForumService: Database connection is null. Cannot perform operations.");
        }
    }

    // CREATE
    public void addForum(Forum f) {
        if (cnx == null) {
            System.err.println("ForumService: Database connection is null. Cannot perform operations.");
            return;
        }
        String sql = "INSERT INTO forum (titre, description, idUser) VALUES (?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, f.getTitre());
            ps.setString(2, f.getDescription());
            ps.setInt(3, f.getIdUser());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // READ ALL
    public List<Forum> getAll() {
        List<Forum> list = new ArrayList<>();
        
        if (cnx == null) {
            System.err.println("ForumService: Database connection is null. Cannot perform operations.");
            return list;
        }
        
        String sql = "SELECT * FROM forum";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Forum f = new Forum();
                f.setIdForum(rs.getInt("idForum"));
                f.setTitre(rs.getString("titre"));
                f.setDescription(rs.getString("description"));
                f.setDateCreation(rs.getTimestamp("dateCreation").toLocalDateTime());
                f.setIdUser(rs.getInt("idUser"));

                list.add(f);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // UPDATE
    public void updateForum(Forum f) {
        if (cnx == null) {
            System.err.println("ForumService: Database connection is null. Cannot perform operations.");
            return;
        }
        
        String sql = "UPDATE forum SET titre=?, description=? WHERE idForum=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, f.getTitre());
            ps.setString(2, f.getDescription());
            ps.setInt(3, f.getIdForum());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // DELETE
    public void deleteForum(int id) {
        if (cnx == null) {
            System.err.println("ForumService: Database connection is null. Cannot perform operations.");
            return;
        }
        
        String sql = "DELETE FROM forum WHERE idForum=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // GET FAVORITES FOR USER
    public List<Forum> getFavoritesForUser(int userId) {
        List<Forum> list = new ArrayList<>();
        
        System.out.println("ForumService: Getting favorites for user ID: " + userId);
        
        if (cnx == null) {
            System.err.println("ForumService: Database connection is null. Cannot perform operations.");
            return list;
        }
        
        String sql = "SELECT f.* FROM forum f " +
                    "INNER JOIN favorites fav ON f.idForum = fav.idForum " +
                    "WHERE fav.idUser = ? " +
                    "ORDER BY fav.dateAdded DESC";

        System.out.println("ForumService: Executing SQL: " + sql);

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    Forum forum = new Forum();
                    forum.setIdForum(rs.getInt("idForum"));
                    forum.setTitre(rs.getString("titre"));
                    forum.setDescription(rs.getString("description"));
                    forum.setDateCreation(rs.getTimestamp("dateCreation").toLocalDateTime());
                    forum.setIdUser(rs.getInt("idUser"));
                    list.add(forum);
                    count++;
                    System.out.println("ForumService: Found favorite - " + forum.getTitre());
                }
                System.out.println("ForumService: Total favorites found: " + count);
            }

        } catch (SQLException e) {
            System.err.println("ForumService: Error getting favorites: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}