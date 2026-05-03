package entity;

import java.time.LocalDateTime;

public class Forum {

    private int idForum;
    private String titre;
    private String description;
    private LocalDateTime dateCreation;
    private int idUser;

    public Forum() {}

    public Forum(String titre, String description, int idUser) {
        this.titre = titre;
        this.description = description;
        this.idUser = idUser;
        this.dateCreation = LocalDateTime.now();
    }

    // GETTERS & SETTERS

    public int getIdForum() { return idForum; }
    public void setIdForum(int idForum) { this.idForum = idForum; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
}