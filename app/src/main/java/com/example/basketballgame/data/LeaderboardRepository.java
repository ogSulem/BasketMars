package com.example.basketballgame.data;

import com.example.basketballgame.GameMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

import androidx.annotation.Nullable;

/**
 * Репозиторий лидерборда — объединяет локальную Room БД и облачный Firestore.
 *
 * <p>Если пользователь авторизован ({@code userId != null}) и Firestore доступен,
 * счёт дополнительно сохраняется в облаке, а при запросе топа возвращаются
 * объединённые результаты (cloud + local, дедуплицированные по имени игрока).</p>
 */
public class LeaderboardRepository {

    public interface ListCallback<T> {
        void onResult(T data);
    }

    public interface StatsUpdater {
        void apply(PlayerStats stats);
    }

    private final LeaderboardDao leaderboardDao;
    private final PlayerStatsDao playerStatsDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    private CloudLeaderboardRepository cloudRepo;

    public LeaderboardRepository(LeaderboardDao leaderboardDao, PlayerStatsDao playerStatsDao) {
        this.leaderboardDao = leaderboardDao;
        this.playerStatsDao = playerStatsDao;
    }

    /** Подключить облачный репозиторий (вызывается из Application после инициализации Firebase). */
    public void setCloudRepository(@Nullable CloudLeaderboardRepository cloud) {
        this.cloudRepo = cloud;
    }

    // ─────────────────────────────────────────────── Сохранение счёта ────

    /**
     * Сохранить счёт игрока локально, и — если передан userId и облако доступно — в Firestore.
     *
     * @param mode       режим игры (GameMode.name())
     * @param score      счёт
     * @param playerName имя игрока
     * @param userId     Firebase UID или null (только локальное сохранение)
     * @param onDone     callback на IO-потоке по завершении (может быть null)
     */
    public void saveScore(String mode, int score, String playerName,
                          @Nullable String userId, @Nullable Runnable onDone) {
        ioExecutor.execute(() -> {
            // Локально — всегда
            Integer best = leaderboardDao.getBestScore(mode, playerName);
            if (best == null || score > best) {
                LeaderboardEntry entry = new LeaderboardEntry();
                entry.mode = mode;
                entry.score = score;
                entry.timestamp = System.currentTimeMillis();
                entry.playerName = playerName;
                leaderboardDao.insert(entry);
            }

            // В облако — если авторизован и Firestore доступен
            if (userId != null && cloudRepo != null && cloudRepo.isAvailable()) {
                cloudRepo.saveScore(mode, score, playerName, userId);
            }

            if (onDone != null) onDone.run();
        });
    }

    /** Перегрузка без onDone-callback: сохранить с userId, без уведомления о завершении. */
    public void saveScore(String mode, int score, String playerName, @Nullable String userId) {
        saveScore(mode, score, playerName, userId, null);
    }

    /** Перегрузка без userId (только локально). */
    public void saveScore(String mode, int score, String playerName, @Nullable Runnable onDone) {
        saveScore(mode, score, playerName, null, onDone);
    }

    // ──────────────────────────────────────────── Получение топ-списка ────

    /**
     * Вернуть топ-N записей.
     *
     * <p>Если Firestore доступен — объединяет облачные и локальные данные,
     * оставляя лучший результат на игрока. Иначе — только локальные данные.</p>
     */
    public void getTopScores(String mode, int limit,
                             ListCallback<List<LeaderboardEntry>> callback) {
        if (cloudRepo != null && cloudRepo.isAvailable()) {
            // Загружаем облачные данные, потом объединяем с локальными
            cloudRepo.getTopScores(mode, limit, cloudEntries ->
                    ioExecutor.execute(() -> {
                        List<LeaderboardEntry> local = leaderboardDao.getTop(mode, limit);
                        List<LeaderboardEntry> merged = merge(local, cloudEntries, limit);
                        if (callback != null) callback.onResult(merged);
                    }));
        } else {
            ioExecutor.execute(() -> {
                List<LeaderboardEntry> entries = leaderboardDao.getTop(mode, limit);
                if (callback != null) callback.onResult(entries);
            });
        }
    }

    /** Объединить локальные и облачные записи: дедупликация по playerName, сортировка по score. */
    private List<LeaderboardEntry> merge(List<LeaderboardEntry> local,
                                         List<LeaderboardEntry> cloud, int limit) {
        java.util.Map<String, LeaderboardEntry> best = new java.util.LinkedHashMap<>();

        for (LeaderboardEntry e : cloud) {
            if (e.playerName != null) best.put(e.playerName, e);
        }
        for (LeaderboardEntry e : local) {
            if (e.playerName == null) continue;
            LeaderboardEntry existing = best.get(e.playerName);
            if (existing == null || e.score > existing.score) {
                best.put(e.playerName, e);
            }
        }

        List<LeaderboardEntry> result = new ArrayList<>(best.values());
        result.sort((a, b) -> Integer.compare(b.score, a.score));
        return result.subList(0, Math.min(result.size(), limit));
    }

    // ────────────────────────────────────────────── Статистика игрока ────

    public void updateStats(StatsUpdater update, @Nullable Runnable onDone) {
        ioExecutor.execute(() -> {
            PlayerStats stats = playerStatsDao.getStats();
            if (stats == null) stats = new PlayerStats();
            if (update != null) update.apply(stats);
            stats.updatedAt = System.currentTimeMillis();
            playerStatsDao.save(stats);
            if (onDone != null) onDone.run();
        });
    }

    public void getStats(ListCallback<PlayerStats> callback) {
        ioExecutor.execute(() -> {
            PlayerStats stats = playerStatsDao.getStats();
            if (stats == null) stats = new PlayerStats();
            if (callback != null) callback.onResult(stats);
        });
    }

    // ──────────────────────────────────────────── Демо-данные при старте ─

    private void seedDemoDataIfEmptyInternal() {
        try {
            if (leaderboardDao.countAll() > 0) return;
        } catch (Exception ignored) {
            return;
        }

        String[] names = {"Mars", "Tim", "Den", "Alina", "Sasha",
                "Ilya", "Nika", "Oleg", "Rita", "Kostya"};
        Random r = new Random(42);

        for (String n : names) {
            insertDemo(n, GameMode.ARCADE.name(),      8 + r.nextInt(55));
            insertDemo(n, GameMode.TIMED.name(),       6 + r.nextInt(45));
            insertDemo(n, GameMode.ONLINE_DUEL.name(), 5 + r.nextInt(50));
            insertDemo(n, GameMode.ONLINE_PVP.name(),  4 + r.nextInt(48));
        }
    }

    private void insertDemo(String name, String mode, int score) {
        LeaderboardEntry e = new LeaderboardEntry();
        e.mode = mode;
        e.score = score;
        e.timestamp = System.currentTimeMillis();
        e.playerName = name;
        leaderboardDao.insert(e);
    }

    public void seedDemoDataIfEmpty() {
        ioExecutor.execute(this::seedDemoDataIfEmptyInternal);
    }

    public void seedDemoDataIfEmpty(Runnable onDone) {
        ioExecutor.execute(() -> {
            seedDemoDataIfEmptyInternal();
            if (onDone != null) onDone.run();
        });
    }
}
