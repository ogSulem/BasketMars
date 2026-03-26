package com.example.basketballgame.data;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit-тесты для POJO-сущности PlayerStats.
 * Проверяют начальные значения и корректность полей.
 */
public class PlayerStatsTest {

    @Test
    public void defaultValues_areZero() {
        PlayerStats stats = new PlayerStats();
        assertEquals(0, stats.totalGames);
        assertEquals(0, stats.arcadeBest);
        assertEquals(0, stats.timedBest);
        assertEquals(0, stats.duelBest);
        assertEquals(0, stats.duelWins);
        assertEquals(0, stats.duelLosses);
    }

    @Test
    public void id_defaultIsSingleton() {
        PlayerStats stats = new PlayerStats();
        assertEquals(1L, stats.id);
    }

    @Test
    public void arcadeBest_updatesCorrectly() {
        PlayerStats stats = new PlayerStats();
        stats.arcadeBest = 42;
        assertEquals(42, stats.arcadeBest);
    }

    @Test
    public void duelWinsAndLosses_areIndependent() {
        PlayerStats stats = new PlayerStats();
        stats.duelWins = 5;
        stats.duelLosses = 3;
        assertEquals(5, stats.duelWins);
        assertEquals(3, stats.duelLosses);
    }

    @Test
    public void totalGames_incrementsCorrectly() {
        PlayerStats stats = new PlayerStats();
        stats.totalGames++;
        stats.totalGames++;
        assertEquals(2, stats.totalGames);
    }

    @Test
    public void bestScore_updatesOnlyIfGreater() {
        PlayerStats stats = new PlayerStats();
        int newScore = 15;
        if (newScore > stats.arcadeBest) {
            stats.arcadeBest = newScore;
        }
        assertEquals(15, stats.arcadeBest);

        int lowerScore = 5;
        if (lowerScore > stats.arcadeBest) {
            stats.arcadeBest = lowerScore;
        }
        // Должно остаться 15, а не 5
        assertEquals(15, stats.arcadeBest);
    }
}
