package org.example.services;

import org.example.entities.Centre;
import org.example.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO / Service for the Centre entity.
 * Full CRUD against the `centers` table in esport_db.
 */
public class CentreService {

    // ── CREATE ────────────────────────────────────────────────────────────────

    public void ajouter(Centre c) throws SQLException {
        String sql = "INSERT INTO centers (name, address, city, contact_email, map_url) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getAddress());
            ps.setString(3, c.getCity());
            ps.setString(4, c.getContactEmail());
            ps.setString(5, c.getMapUrl());
            ps.executeUpdate();
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public void modifier(Centre c) throws SQLException {
        String sql = "UPDATE centers SET name=?, address=?, city=?, " +
                     "contact_email=?, map_url=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getAddress());
            ps.setString(3, c.getCity());
            ps.setString(4, c.getContactEmail());
            ps.setString(5, c.getMapUrl());
            ps.setInt   (6, c.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM centers WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────

    public List<Centre> listerTous() throws SQLException {
        List<Centre> liste = new ArrayList<>();
        String sql = "SELECT * FROM centers ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────

    public Centre trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM centers WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private Centre mapRow(ResultSet rs) throws SQLException {
        return new Centre(
                rs.getInt   ("id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("contact_email"),
                rs.getString("map_url")
        );
    }
}
