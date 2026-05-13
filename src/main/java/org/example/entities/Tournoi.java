package org.example.entities;

import java.time.LocalDate;

public class Tournoi {

    private int id;
    private String nom;
    private String jeu;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String lieu;
    private double prix;
    private int nbEquipes;
    /** FK → centres.id  (0 = not assigned) */
    private int centreId;

    public Tournoi() {}

    public Tournoi(String nom, String jeu, LocalDate dateDebut, LocalDate dateFin, String lieu, double prix, int nbEquipes) {
        this.nom = nom;
        this.jeu = jeu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.prix = prix;
        this.nbEquipes = nbEquipes;
    }

    public Tournoi(String nom, String jeu, LocalDate dateDebut, LocalDate dateFin, String lieu, double prix, int nbEquipes, int centreId) {
        this(nom, jeu, dateDebut, dateFin, lieu, prix, nbEquipes);
        this.centreId = centreId;
    }

    public Tournoi(int id, String nom, String jeu, LocalDate dateDebut, LocalDate dateFin, String lieu, double prix, int nbEquipes) {
        this.id = id;
        this.nom = nom;
        this.jeu = jeu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.prix = prix;
        this.nbEquipes = nbEquipes;
    }

    public Tournoi(int id, String nom, String jeu, LocalDate dateDebut, LocalDate dateFin, String lieu, double prix, int nbEquipes, int centreId) {
        this(id, nom, jeu, dateDebut, dateFin, lieu, prix, nbEquipes);
        this.centreId = centreId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getJeu() { return jeu; }
    public void setJeu(String jeu) { this.jeu = jeu; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getNbEquipes() { return nbEquipes; }
    public void setNbEquipes(int nbEquipes) { this.nbEquipes = nbEquipes; }

    public int getCentreId() { return centreId; }
    public void setCentreId(int centreId) { this.centreId = centreId; }

    @Override
    public String toString() {
        return nom + " (" + jeu + ")";
    }
}