package service;



import entity.Tournoi;
import utilies.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournoiService {

    public void ajouter(Tournoi t) throws SQLException {
        String sql = "INSERT INTO tournoi (nom, jeu, date_debut, date_fin, lieu, prix, nb_equipes, centre_id) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getNom());
            ps.setString(2, t.getJeu());
            ps.setDate  (3, Date.valueOf(t.getDateDebut()));
            ps.setDate  (4, Date.valueOf(t.getDateFin()));
            ps.setString(5, t.getLieu());
            ps.setDouble(6, t.getPrix());
            ps.setInt   (7, t.getNbEquipes());
            if (t.getCentreId() > 0) ps.setInt(8, t.getCentreId());
            else                      ps.setNull(8, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public void modifier(Tournoi t) throws SQLException {
        String sql = "UPDATE tournoi SET nom=?, jeu=?, date_debut=?, date_fin=?, lieu=?, prix=?, nb_equipes=?, centre_id=? WHERE id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getNom());
            ps.setString(2, t.getJeu());
            ps.setDate  (3, Date.valueOf(t.getDateDebut()));
            ps.setDate  (4, Date.valueOf(t.getDateFin()));
            ps.setString(5, t.getLieu());
            ps.setDouble(6, t.getPrix());
            ps.setInt   (7, t.getNbEquipes());
            if (t.getCentreId() > 0) ps.setInt(8, t.getCentreId());
            else                      ps.setNull(8, Types.INTEGER);
            ps.setInt   (9, t.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM tournoi WHERE id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Tournoi> listerTous() throws SQLException {
        List<Tournoi> liste = new ArrayList<>();
        String sql = "SELECT * FROM tournoi";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    /** Returns only the tournois that belong to a given centre. */
    public List<Tournoi> listerParCentre(int centreId) throws SQLException {
        List<Tournoi> liste = new ArrayList<>();
        String sql = "SELECT * FROM tournoi WHERE centre_id=?";
        try (Connection con =  DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, centreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    private Tournoi mapRow(ResultSet rs) throws SQLException {
        int centreId = rs.getInt("centre_id"); // 0 if NULL
        return new Tournoi(
                rs.getInt   ("id"),
                rs.getString("nom"),
                rs.getString("jeu"),
                rs.getDate  ("date_debut").toLocalDate(),
                rs.getDate  ("date_fin")  .toLocalDate(),
                rs.getString("lieu"),
                rs.getDouble("prix"),
                rs.getInt   ("nb_equipes"),
                centreId
        );
    }
}