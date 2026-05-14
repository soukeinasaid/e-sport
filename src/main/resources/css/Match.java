package org.example.entities;

import java.time.LocalDateTime;

public class Match {
    private int id;
    private int tournoiId;
    private int round;
    private int matchOrder;
    private Integer team1Id; // nullable
    private Integer team2Id; // nullable
    private Integer winnerId; // nullable
    private Status status;
    private LocalDateTime scheduledAt;

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    // Constructors
    public Match() {}

    public Match(int tournoiId, int round, int matchOrder) {
        this.tournoiId = tournoiId;
        this.round = round;
        this.matchOrder = matchOrder;
        this.status = Status.PENDING;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTournoiId() {
        return tournoiId;
    }

    public void setTournoiId(int tournoiId) {
        this.tournoiId = tournoiId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getMatchOrder() {
        return matchOrder;
    }

    public void setMatchOrder(int matchOrder) {
        this.matchOrder = matchOrder;
    }

    public Integer getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(Integer team1Id) {
        this.team1Id = team1Id;
    }

    public Integer getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(Integer team2Id) {
        this.team2Id = team2Id;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    @Override
    public String toString() {
        return "Match{" +
                "id=" + id +
                ", tournoiId=" + tournoiId +
                ", round=" + round +
                ", matchOrder=" + matchOrder +
                ", team1Id=" + team1Id +
                ", team2Id=" + team2Id +
                ", winnerId=" + winnerId +
                ", status=" + status +
                ", scheduledAt=" + scheduledAt +
                '}';
    }
}
