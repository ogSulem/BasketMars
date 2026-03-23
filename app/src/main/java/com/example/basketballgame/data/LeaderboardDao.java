package com.example.basketballgame.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LeaderboardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LeaderboardEntry entry);

    @Query("SELECT playerName, mode, MAX(score) AS score, MIN(timestamp) AS timestamp, 0 AS id " +
            "FROM leaderboard_entries WHERE mode = :mode GROUP BY playerName, mode " +
            "ORDER BY score DESC, timestamp ASC LIMIT :limit")
    List<LeaderboardEntry> getTop(String mode, int limit);

    @Query("SELECT MAX(score) FROM leaderboard_entries WHERE mode = :mode AND playerName = :playerName")
    Integer getBestScore(String mode, String playerName);

    @Query("SELECT COUNT(*) FROM leaderboard_entries")
    int countAll();

    @Query("DELETE FROM leaderboard_entries")
    void deleteAll();
}
