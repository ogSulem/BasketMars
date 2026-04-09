package com.example.basketballgame.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "player_stats")
public class PlayerStats {
    @PrimaryKey
    public long id = 1;

    public int totalGames;
    public int arcadeBest;
    public int timedBest;
    public int duelBest;
    public int duelWins;
    public int duelLosses;

    public int onlinePvpBest;
    public int onlinePvpWins;
    public int onlinePvpLosses;

    public long updatedAt;
}
