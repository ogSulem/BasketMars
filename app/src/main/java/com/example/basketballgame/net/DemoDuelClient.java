package com.example.basketballgame.net;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.Random;

public class DemoDuelClient implements MatchClient {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Listener listener;
    private final Random random = new Random();

    private boolean connected;
    private boolean running;

    private float ghostX;
    private float ghostY;
    private float targetX;
    private float targetY;
    private float vx;
    private float vy;
    private boolean moving;

    private int remoteScore;
    private int lastPlayerScore;
    private int lastPlayerScoreForRate;
    private long lastPlayerRateAtMs;
    private float playerScorePerSec;
    private long lastStepMs;
    private long nextScoreAtMs;
    private int missStreak;
    private int scoreStreak;

    public DemoDuelClient(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void connect(String target) {
        if (connected) return;
        connected = true;
        running = true;
        initState();
        listener.onConnected();
        scheduleStep();
    }

    @Override
    public void sendSnapshot(float x, float y, boolean moving, int score) {
        lastPlayerScore = score;
        if (!connected) return;

        long now = SystemClock.uptimeMillis();
        long dtMs = now - lastPlayerRateAtMs;
        if (dtMs >= 700) {
            int dScore = lastPlayerScore - lastPlayerScoreForRate;
            float dt = Math.max(0.001f, dtMs / 1000f);
            float instRate = dScore / dt;
            playerScorePerSec = 0.75f * playerScorePerSec + 0.25f * instRate;
            lastPlayerScoreForRate = lastPlayerScore;
            lastPlayerRateAtMs = now;
        }

        // «Человечная» реакция: чаще подстраиваем цель рядом с игроком, но не дёргаемся каждый кадр.
        if (random.nextFloat() < 0.14f) {
            float followAmp = 180f + Math.min(140f, playerScorePerSec * 55f);
            targetX = clamp(x + jitter(followAmp), 0f, 1080f);
            targetY = clamp(y + jitter(followAmp), 0f, 2400f);
        }
    }

    @Override
    public void disconnect() {
        running = false;
        connected = false;
        handler.removeCallbacksAndMessages(null);
        listener.onDisconnected();
    }

    private void initState() {
        lastStepMs = SystemClock.uptimeMillis();
        lastPlayerRateAtMs = lastStepMs;
        lastPlayerScoreForRate = lastPlayerScore;
        playerScorePerSec = 0f;
        ghostX = 540f;
        ghostY = 1800f;
        pickNewTarget();
        moving = false;
        remoteScore = 0;
        missStreak = 0;
        scoreStreak = 0;
        nextScoreAtMs = lastStepMs + 2000L + random.nextInt(1500);
    }

    private void scheduleStep() {
        handler.postDelayed(this::step, 16);
    }

    private void step() {
        if (!running || !connected) return;
        long now = SystemClock.uptimeMillis();
        float dt = Math.max(0.001f, (now - lastStepMs) / 1000f);
        lastStepMs = now;

        float dx = targetX - ghostX;
        float dy = targetY - ghostY;
        float dist2 = dx * dx + dy * dy;

        if (dist2 < 40f * 40f) {
            pickNewTarget();
        }

        float ax = (dx * 0.8f) - vx * 1.6f;
        float ay = (dy * 0.8f) - vy * 1.6f;
        vx += ax * dt;
        vy += ay * dt;

        float maxV = 900f;
        vx = clamp(vx, -maxV, maxV);
        vy = clamp(vy, -maxV, maxV);

        ghostX += vx * dt;
        ghostY += vy * dt;

        ghostX = clamp(ghostX, 0f, 1080f);
        ghostY = clamp(ghostY, 0f, 2400f);

        moving = (Math.abs(vx) + Math.abs(vy)) > 40f;

        if (now >= nextScoreAtMs) {
            // Цель: бот играет «примерно рядом», но может делать серии и промахи.
            float pace = Math.max(0f, Math.min(3.2f, playerScorePerSec));

            // Серии: если подряд забил 2-3 раза — шанс на промах растёт.
            float missChance = 0.16f + 0.06f * scoreStreak - 0.04f * missStreak;
            missChance = clamp(missChance, 0.12f, 0.52f);

            boolean miss = random.nextFloat() < missChance;
            if (miss) {
                missStreak++;
                scoreStreak = 0;
            } else {
                missStreak = 0;
                scoreStreak++;
                int delta = 1;
                // Иногда «двушка» на хорошем темпе игрока.
                float twoChance = 0.08f + 0.06f * pace;
                if (random.nextFloat() < Math.min(0.28f, twoChance)) {
                    delta = 2;
                }
                remoteScore += delta;
            }

            long base = (long) (2550L - pace * 480L);
            // Если бот отстаёт — немного ускоряемся, если лидирует — замедляемся.
            int diff = lastPlayerScore - remoteScore;
            if (diff > 4) base -= 220L;
            if (diff < -4) base += 260L;

            // После промаха задержка обычно больше.
            if (miss) base += 420L + random.nextInt(350);

            base = (long) clamp(base, 800L, 3200L);
            nextScoreAtMs = now + base + random.nextInt(500);
        }

        listener.onGhostSnapshot(ghostX, ghostY, moving, remoteScore);
        scheduleStep();
    }

    private void pickNewTarget() {
        targetX = 140f + random.nextFloat() * 800f;
        targetY = 320f + random.nextFloat() * 1500f;
    }

    private float jitter(float amp) {
        return (random.nextFloat() - 0.5f) * 2f * amp;
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
