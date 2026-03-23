package com.example.basketballgame.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Room база данных для хранения лидербордов и статистики игрока.
 */
@Database(
    entities = {LeaderboardEntry.class, PlayerStats.class},
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LeaderboardDao leaderboardDao();
    public abstract PlayerStatsDao playerStatsDao();
}
