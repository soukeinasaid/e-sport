package service;


import entity.Team;
import utilies.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO / Service for the Team entity.
 * Full CRUD against the `teams` table in esport_db.
 */
public class TeamService {

    // ── CREATE ────────────────────────────────────────────────────────────────

    public void ajouter(Team t) throws SQLException {
        String sql = "INSERT INTO teams (name, tournoi_id, captain_id) VALUES (?, ?, ?)";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setInt(2, t.getTournoiId());
            ps.setInt(3, t.getCaptainId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public void modifier(Team t) throws SQLException {
        String sql = "UPDATE teams SET name=?, tournoi_id=?, captain_id=? WHERE id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setInt(2, t.getTournoiId());
            ps.setInt(3, t.getCaptainId());
            ps.setInt(4, t.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM teams WHERE id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────

    public List<Team> listerTous() throws SQLException {
        List<Team> liste = new ArrayList<>();
        String sql = "SELECT * FROM teams ORDER BY name";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    // ── READ BY TOURNOI ───────────────────────────────────────────────────────

    public List<Team> listerParTournoi(int tournoiId) throws SQLException {
        List<Team> liste = new ArrayList<>();
        String sql = "SELECT * FROM teams WHERE tournoi_id=? ORDER BY name";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tournoiId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapRow(rs));
                }
            }
        }
        return liste;
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────

    public Team trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM teams WHERE id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── ADD PLAYER TO TEAM ────────────────────────────────────────────────────

    public void ajouterJoueur(int teamId, int userId) throws SQLException {
        String sql = "INSERT INTO team_members (team_id, user_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE joined_at=joined_at";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ── REMOVE PLAYER FROM TEAM ───────────────────────────────────────────────

    public void supprimerJoueur(int teamId, int userId) throws SQLException {
        String sql = "DELETE FROM team_members WHERE team_id=? AND user_id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ── GET TEAM MEMBERS ──────────────────────────────────────────────────────

    public List<Integer> getMembres(int teamId) throws SQLException {
        List<Integer> membres = new ArrayList<>();
        String sql = "SELECT user_id FROM team_members WHERE team_id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    membres.add(rs.getInt("user_id"));
                }
            }
        }
        return membres;
    }

    // ── GET TEAM STATS ──────────────────────────────────────────────────────

    public int getWins(int teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matches WHERE (team1_id=? OR team2_id=?) AND winner_id=? AND status='COMPLETED'";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            ps.setInt(3, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getLosses(int teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matches WHERE ((team1_id=? OR team2_id=?) AND winner_id IS NOT NULL AND winner_id != ? AND status='COMPLETED')";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            ps.setInt(3, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getMatchesPlayed(int teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matches WHERE (team1_id=? OR team2_id=?) AND status='COMPLETED'";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private Team mapRow(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setTournoiId(rs.getInt("tournoi_id"));
        t.setCaptainId(rs.getInt("captain_id"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return t;
    }
}
