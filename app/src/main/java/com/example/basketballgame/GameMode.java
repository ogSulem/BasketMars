package com.example.basketballgame;

/**
 * Перечисление доступных игровых режимов.
 * ARCADE      — бесконечный набор очков с возрастающей сложностью.
 * TIMED       — ограничение по времени (60 секунд).
 * ONLINE_DUEL — дуэль против умного бота (название сохранено для совместимости с Room-базой).
 * ONLINE_PVP  — дуэль против реального игрока через Firebase Firestore.
 */
public enum GameMode {
    ARCADE,
    TIMED,
    ONLINE_DUEL,
    ONLINE_PVP;

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
