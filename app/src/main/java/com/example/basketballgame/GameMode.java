package com.example.basketballgame;

/**
 * Перечисление доступных игровых режимов.
 * ARCADE      — бесконечный набор очков с возрастающей сложностью.
 * TIMED       — ограничение по времени.
 * ONLINE_DUEL — онлайн матч 1 на 1 с призрачным соперником.
 */
public enum GameMode {
    ARCADE,
    TIMED,
    ONLINE_DUEL;

    public static final String EXTRA_KEY = "com.example.basketballgame.EXTRA_GAME_MODE";

    public static GameMode fromName(String name) {
        if (name == null) return ARCADE;
        try {
            return GameMode.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return ARCADE;
        }
    }
}
