package entity;

public class User {

    public enum Role {
        ADMIN,
        USER
    }

    private int idUser;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private Role role = Role.USER; // Default to USER

    public User() {}

    // constructeur sans id (pour insertion)
    public User(String nom, String prenom, String email, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = Role.USER; // Default role
    }

    // constructeur sans id (avec role)
    public User(String nom, String prenom, String email, String motDePasse, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // constructeur complet
    public User(int idUser, String nom, String prenom, String email, String motDePasse, Role role) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // constructeur complet (backward compatibility)
    public User(int idUser, String nom, String prenom, String email, String motDePasse) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = Role.USER;
    }

    // GETTERS & SETTERS

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public String getRoleString() {
        return role.toString();
    }

    public void setRoleFromString(String roleString) {
        try {
            this.role = Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.role = Role.USER; // Default to USER if invalid
        }
    }
}