package org.example.entities;

/**
 * Entity : Centre
 * Represents a physical gaming / e-sport centre that hosts tournaments.
 * Relation : one Centre  →  many Tournois  (stored via tournoi.centre_id FK)
 */
public class Centre {

    private int    id;
    private String name;
    private String address;
    private String city;
    private String contactEmail;
    private String mapUrl;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Centre() {}

    /** Used when inserting (no id yet). */
    public Centre(String name, String address, String city,
                  String contactEmail, String mapUrl) {
        this.name         = name;
        this.address      = address;
        this.city         = city;
        this.contactEmail = contactEmail;
        this.mapUrl       = mapUrl;
    }

    /** Used when reading from DB (id known). */
    public Centre(int id, String name, String address, String city,
                  String contactEmail, String mapUrl) {
        this.id           = id;
        this.name         = name;
        this.address      = address;
        this.city         = city;
        this.contactEmail = contactEmail;
        this.mapUrl       = mapUrl;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getId()           { return id; }
    public void   setId(int id)     { this.id = id; }

    public String getName()         { return name; }
    public void   setName(String n) { this.name = n; }

    public String getAddress()              { return address; }
    public void   setAddress(String a)      { this.address = a; }

    public String getCity()                 { return city; }
    public void   setCity(String c)         { this.city = c; }

    public String getContactEmail()         { return contactEmail; }
    public void   setContactEmail(String e) { this.contactEmail = e; }

    public String getMapUrl()               { return mapUrl; }
    public void   setMapUrl(String u)       { this.mapUrl = u; }

    @Override
    public String toString() {
        return name + " (" + city + ")";
    }
}
