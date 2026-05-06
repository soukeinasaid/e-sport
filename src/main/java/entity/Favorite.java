package entity;

import java.time.LocalDateTime;

public class Favorite {

    private int idFavorite;
    private int idForum;
    private int idUser;
    private LocalDateTime dateAdded;

    public Favorite() {}

    public Favorite(int idForum, int idUser) {
        this.idForum = idForum;
        this.idUser = idUser;
        this.dateAdded = LocalDateTime.now();
    }

    // GETTERS & SETTERS

    public int getIdFavorite() { return idFavorite; }
    public void setIdFavorite(int idFavorite) { this.idFavorite = idFavorite; }

    public int getIdForum() { return idForum; }
    public void setIdForum(int idForum) { this.idForum = idForum; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }
}
