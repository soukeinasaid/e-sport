package com.victorygrid.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String role; // "PLAYER", "ADMIN"
    private String rank; // "Bronze", "Silver", "Gold", "Platinum", "Diamond"
    private int wins;
    private int losses;
    private double winRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
        this.wins = 0;
        this.losses = 0;
        this.winRate = 0.0;
    }

    public User(String username, String email, String password, String role, String rank) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.rank = rank;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
        this.updatedAt = LocalDateTime.now();
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
        updateWinRate();
        this.updatedAt = LocalDateTime.now();
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
        updateWinRate();
        this.updatedAt = LocalDateTime.now();
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
        this.updatedAt = LocalDateTime.now();
    }

    private void updateWinRate() {
        int totalGames = wins + losses;
        if (totalGames > 0) {
            this.winRate = (double) wins / totalGames * 100;
        } else {
            this.winRate = 0.0;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", rank='" + rank + '\'' +
                ", wins=" + wins +
                ", losses=" + losses +
                ", winRate=" + winRate +
                ", active=" + active +
                '}';
    }
}
