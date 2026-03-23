package com.example.basketballgame.data;

import com.example.basketballgame.GameMode;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

/**
 * Репозиторий инкапсулирует работу с Room в фоновых потоках.
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

    public LeaderboardRepository(LeaderboardDao leaderboardDao, PlayerStatsDao playerStatsDao) {
        this.leaderboardDao = leaderboardDao;
        this.playerStatsDao = playerStatsDao;
    }

    public void saveScore(String mode, int score, String playerName, Runnable onDone) {
        ioExecutor.execute(() -> {
            Integer best = leaderboardDao.getBestScore(mode, playerName);
            if (best == null || score > best) {
                LeaderboardEntry entry = new LeaderboardEntry();
                entry.mode = mode;
                entry.score = score;
                entry.timestamp = System.currentTimeMillis();
                entry.playerName = playerName;
                leaderboardDao.insert(entry);
            }
            if (onDone != null) onDone.run();
        });
    }

    public void getTopScores(String mode, int limit, ListCallback<List<LeaderboardEntry>> callback) {
        ioExecutor.execute(() -> {
            List<LeaderboardEntry> entries = leaderboardDao.getTop(mode, limit);
            if (callback != null) callback.onResult(entries);
        });
    }

    public void updateStats(StatsUpdater update, Runnable onDone) {
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

    private void seedDemoDataIfEmptyInternal() {
        try {
            if (leaderboardDao.countAll() > 0) return;
        } catch (Exception ignored) {
            return;
        }

        String[] names = new String[]{
                "Mars", "Tim", "Den", "Alina", "Sasha", "Ilya", "Nika", "Oleg", "Rita", "Kostya"
        };
        Random r = new Random(42);

        for (String n : names) {
            int arcade = 8 + r.nextInt(55);
            int timed = 6 + r.nextInt(45);
            int duel = 5 + r.nextInt(50);

            LeaderboardEntry e1 = new LeaderboardEntry();
            e1.mode = GameMode.ARCADE.name();
            e1.score = arcade;
            e1.timestamp = System.currentTimeMillis();
            e1.playerName = n;
            leaderboardDao.insert(e1);

            LeaderboardEntry e2 = new LeaderboardEntry();
            e2.mode = GameMode.TIMED.name();
            e2.score = timed;
            e2.timestamp = System.currentTimeMillis();
            e2.playerName = n;
            leaderboardDao.insert(e2);

            LeaderboardEntry e3 = new LeaderboardEntry();
            e3.mode = GameMode.ONLINE_DUEL.name();
            e3.score = duel;
            e3.timestamp = System.currentTimeMillis();
            e3.playerName = n;
            leaderboardDao.insert(e3);
        }
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
