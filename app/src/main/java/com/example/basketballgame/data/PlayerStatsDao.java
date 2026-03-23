package com.example.basketballgame.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    PlayerStats getStats();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(PlayerStats stats);
}
