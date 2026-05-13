package org.example.entities;

import java.time.LocalDateTime;

public class Team {
    private int id;
    private String name;
    private int tournoiId;
    private int captainId;
    private LocalDateTime createdAt;

    // Constructors
    public Team() {}

    public Team(String name, int tournoiId, int captainId) {
        this.name = name;
        this.tournoiId = tournoiId;
        this.captainId = captainId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTournoiId() {
        return tournoiId;
    }

    public void setTournoiId(int tournoiId) {
        this.tournoiId = tournoiId;
    }

    public int getCaptainId() {
        return captainId;
    }

    public void setCaptainId(int captainId) {
        this.captainId = captainId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tournoiId=" + tournoiId +
                ", captainId=" + captainId +
                ", createdAt=" + createdAt +
                '}';
    }
}
