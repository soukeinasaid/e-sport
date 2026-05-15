package service;

import entity.User;
import utilies.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection conn;

    public UserService() {
        conn = DatabaseConfig.getInstance().getConnection();
        if (conn == null) {
            System.err.println("UserService: Database connection is null. Cannot perform operations.");
        }
    }

    // SIGN UP
    public boolean register(User user) {
        if (conn == null) {
            System.err.println("UserService: Database connection is null. Cannot perform operations.");
            return false;
        }

        // Try with profile_picture column first
        String sql = "INSERT INTO utilisateur (nom, prenom, email, motDePasse, role, profile_picture) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMotDePasse());
            ps.setString(5, user.getRoleString());
            ps.setString(6, user.getProfilePicture());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            // If column doesn't exist, try without profile_picture
            if (e.getMessage().contains("Unknown column")) {
                System.out.println("Profile picture column not found, registering without profile picture...");
                String sqlWithoutProfile = "INSERT INTO utilisateur (nom, prenom, email, motDePasse, role) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlWithoutProfile)) {
                    ps.setString(1, user.getNom());
                    ps.setString(2, user.getPrenom());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getMotDePasse());
                    ps.setString(5, user.getRoleString());
                    ps.executeUpdate();
                    return true;
                } catch (SQLException e2) {
                    System.out.println("Erreur register: " + e2.getMessage());
                    return false;
                }
            }
            System.out.println("Erreur register: " + e.getMessage());
            return false;
        }
    }

    // ✅ LOGIN
    public User login(String email, String password) {
        if (conn == null) {
            System.err.println("UserService: Database connection is null. Cannot perform login.");
            return null;
        }

        String sql = "SELECT * FROM utilisateur WHERE email = ? AND motDePasse = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("idUser"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse")
                );
                
                // Set role from database
                String roleString = rs.getString("role");
                if (roleString != null) {
                    user.setRoleFromString(roleString);
                }
                
                // Set profile picture from database (handle missing column gracefully)
                try {
                    String profilePicture = rs.getString("profile_picture");
                    if (profilePicture != null) {
                        user.setProfilePicture(profilePicture);
                    }
                } catch (SQLException e) {
                    // Column doesn't exist yet, skip profile picture
                    System.out.println("Profile picture column not found, skipping...");
                }
                
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ READ ALL
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";

        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                User user = new User(
                        rs.getInt("idUser"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse")
                );
                
                // Set role from database
                String roleString = rs.getString("role");
                if (roleString != null) {
                    user.setRoleFromString(roleString);
                }
                
                // Set profile picture from database (handle missing column gracefully)
                try {
                    String profilePicture = rs.getString("profile_picture");
                    if (profilePicture != null) {
                        user.setProfilePicture(profilePicture);
                    }
                } catch (SQLException e) {
                    // Column doesn't exist yet, skip profile picture
                }
                
                list.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ✅ UPDATE
    public boolean update(User user) {
        // Try with profile_picture column first
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, motDePasse=?, role=?, profile_picture=? WHERE idUser=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMotDePasse());
            ps.setString(5, user.getRoleString());
            ps.setString(6, user.getProfilePicture());
            ps.setInt(7, user.getIdUser());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            // If column doesn't exist, try without profile_picture
            if (e.getMessage().contains("Unknown column")) {
                System.out.println("Profile picture column not found, updating without profile picture...");
                String sqlWithoutProfile = "UPDATE utilisateur SET nom=?, prenom=?, email=?, motDePasse=?, role=? WHERE idUser=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlWithoutProfile)) {
                    ps.setString(1, user.getNom());
                    ps.setString(2, user.getPrenom());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getMotDePasse());
                    ps.setString(5, user.getRoleString());
                    ps.setInt(6, user.getIdUser());
                    ps.executeUpdate();
                    return true;
                } catch (SQLException e2) {
                    e2.printStackTrace();
                    return false;
                }
            }
            e.printStackTrace();
            return false;
        }
    }

    // ✅ DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE idUser=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}