package org.example.entities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TeamStats {
    private final SimpleIntegerProperty rank = new SimpleIntegerProperty();
    private final SimpleStringProperty teamName = new SimpleStringProperty();
    private final SimpleIntegerProperty wins = new SimpleIntegerProperty();
    private final SimpleIntegerProperty losses = new SimpleIntegerProperty();
    private final SimpleIntegerProperty matchesPlayed = new SimpleIntegerProperty();
    private int teamId;

    public TeamStats(int rank, String teamName, int wins, int losses, int matchesPlayed, int teamId) {
        setRank(rank);
        setTeamName(teamName);
        setWins(wins);
        setLosses(losses);
        setMatchesPlayed(matchesPlayed);
        this.teamId = teamId;
    }

    // Getters and setters for properties
    public int getRank() { return rank.get(); }
    public void setRank(int rank) { this.rank.set(rank); }
    public SimpleIntegerProperty rankProperty() { return rank; }

    public String getTeamName() { return teamName.get(); }
    public void setTeamName(String teamName) { this.teamName.set(teamName); }
    public SimpleStringProperty teamNameProperty() { return teamName; }

    public int getWins() { return wins.get(); }
    public void setWins(int wins) { this.wins.set(wins); }
    public SimpleIntegerProperty winsProperty() { return wins; }

    public int getLosses() { return losses.get(); }
    public void setLosses(int losses) { this.losses.set(losses); }
    public SimpleIntegerProperty lossesProperty() { return losses; }

    public int getMatchesPlayed() { return matchesPlayed.get(); }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed.set(matchesPlayed); }
    public SimpleIntegerProperty matchesPlayedProperty() { return matchesPlayed; }

    public int getTeamId() { return teamId; }
}
