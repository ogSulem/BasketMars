package com.example.basketballgame.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "leaderboard_entries")
public class LeaderboardEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String mode;         // GameMode name
    public int score;
    public long timestamp;
    public String playerName;
}
