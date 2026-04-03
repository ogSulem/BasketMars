package com.example.basketballgame.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

/**
 * Room база данных для хранения лидербордов и статистики игрока.
 */
@Database(
    entities = {LeaderboardEntry.class, PlayerStats.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LeaderboardDao leaderboardDao();
    public abstract PlayerStatsDao playerStatsDao();

    /** Миграция 2→3: добавляет поля online PvP в таблицу player_stats. */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE player_stats ADD COLUMN onlinePvpBest INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE player_stats ADD COLUMN onlinePvpWins INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE player_stats ADD COLUMN onlinePvpLosses INTEGER NOT NULL DEFAULT 0");
        }
    };
}
