package com.example.basketballgame;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit-тесты для перечисления GameMode.
 * Проверяют корректность разбора имён режимов (fromName).
 */
public class GameModeTest {

    @Test
    public void fromName_arcade_returnsArcade() {
        assertEquals(GameMode.ARCADE, GameMode.fromName("ARCADE"));
    }

    @Test
    public void fromName_timed_returnsTimed() {
        assertEquals(GameMode.TIMED, GameMode.fromName("TIMED"));
    }

    @Test
    public void fromName_onlineDuel_returnsOnlineDuel() {
        assertEquals(GameMode.ONLINE_DUEL, GameMode.fromName("ONLINE_DUEL"));
    }

    @Test
    public void fromName_null_returnsArcadeDefault() {
        assertEquals(GameMode.ARCADE, GameMode.fromName(null));
    }

    @Test
    public void fromName_emptyString_returnsArcadeDefault() {
        assertEquals(GameMode.ARCADE, GameMode.fromName(""));
    }

    @Test
    public void fromName_unknown_returnsArcadeDefault() {
        assertEquals(GameMode.ARCADE, GameMode.fromName("NONEXISTENT_MODE"));
    }

    @Test
    public void fromName_lowercase_returnsArcadeDefault() {
        // enum.valueOf() чувствителен к регистру — "arcade" не совпадает с ARCADE
        assertEquals(GameMode.ARCADE, GameMode.fromName("arcade"));
    }

    @Test
    public void extraKey_isNotEmpty() {
        assertNotNull(GameMode.EXTRA_KEY);
        assertFalse(GameMode.EXTRA_KEY.isEmpty());
    }

    @Test
    public void values_containsAllThreeModes() {
        GameMode[] modes = GameMode.values();
        assertEquals(3, modes.length);
    }
}
