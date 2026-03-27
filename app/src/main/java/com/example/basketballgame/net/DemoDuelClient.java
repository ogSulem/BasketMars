package com.example.basketballgame.net;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.Random;

/**
 * ИИ-бот для режима дуэли.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Более человечные промахи (базовый шанс промаха ~35 %).</li>
 *   <li>Три типа бросков: прямой, от левой стенки, от правой стенки.</li>
 *   <li>Случайные «холодные серии» (4–7 промахов подряд) и «горячие» мини-серии.</li>
 *   <li>Скорость подстраивается под темп игрока, но никогда не превышает ~2 очка/с.</li>
 * </ul>
 * </p>
 */
public class DemoDuelClient implements MatchClient {

    // Условная ширина и высота экрана (координатное пространство GameView).
    private static final float SCREEN_W = 1080f;
    private static final float SCREEN_H = 2400f;

    // Типы бросков (только для визуальной траектории призрака).
    private static final int SHOT_DIRECT = 0;
    private static final int SHOT_BANK_LEFT = 1;
    private static final int SHOT_BANK_RIGHT = 2;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Listener listener;
    private final Random random = new Random();

    private boolean connected;
    private boolean running;

    // Позиция и скорость призрака
    private float ghostX;
    private float ghostY;
    private float targetX;
    private float targetY;
    private float vx;
    private float vy;
    private boolean moving;

    // Счёт
    private int remoteScore;
    private int lastPlayerScore;

    // Адаптация темпа
    private int lastPlayerScoreForRate;
    private long lastPlayerRateAtMs;
    private float playerScorePerSec;

    // Расписание бросков
    private long lastStepMs;
    private long nextScoreAtMs;

    // Серии
    private int missStreak;       // подряд промахнулся
    private int scoreStreak;      // подряд забил
    private int coldStreak;       // счётчик «холодной серии» (осталось промахнуться ещё N раз)

    // Тип текущего броска и двухфазное движение для банковских бросков
    private int currentShotType = SHOT_DIRECT;
    private boolean bankPhaseOne = false;  // true = ещё едем к стенке
    private float bankWallX;

    public DemoDuelClient(Listener listener) {
        this.listener = listener;
    }

    // ───────────────────────────────────────────────── MatchClient ────────

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
            // EWMA: сглаживаем скачки
            playerScorePerSec = 0.75f * playerScorePerSec + 0.25f * instRate;
            lastPlayerScoreForRate = lastPlayerScore;
            lastPlayerRateAtMs = now;
        }

        // Изредка подстраиваем цель рядом с игроком (человечная реакция).
        if (random.nextFloat() < 0.10f) {
            float amp = 200f + Math.min(160f, playerScorePerSec * 60f);
            float nx = clamp(x + jitter(amp), 0f, SCREEN_W);
            float ny = clamp(y + jitter(amp), 0f, SCREEN_H);
            setTarget(nx, ny);
        }
    }

    @Override
    public void disconnect() {
        running = false;
        connected = false;
        handler.removeCallbacksAndMessages(null);
        listener.onDisconnected();
    }

    // ─────────────────────────────────────────────────── Internals ────────

    private void initState() {
        lastStepMs = SystemClock.uptimeMillis();
        lastPlayerRateAtMs = lastStepMs;
        lastPlayerScoreForRate = 0;
        playerScorePerSec = 0f;
        ghostX = SCREEN_W / 2f;
        ghostY = SCREEN_H * 0.75f;
        pickNewShot();
        moving = false;
        remoteScore = 0;
        missStreak = 0;
        scoreStreak = 0;
        coldStreak = 0;
        nextScoreAtMs = lastStepMs + 2500L + random.nextInt(1500);
    }

    private void scheduleStep() {
        handler.postDelayed(this::step, 16);
    }

    private void step() {
        if (!running || !connected) return;
        long now = SystemClock.uptimeMillis();
        float dt = Math.max(0.001f, (now - lastStepMs) / 1000f);
        lastStepMs = now;

        moveGhost(dt);

        // Пришло время для следующего броска?
        if (now >= nextScoreAtMs) {
            executeShot(now);
        }

        listener.onGhostSnapshot(ghostX, ghostY, moving, remoteScore);
        scheduleStep();
    }

    /**
     * Плавное движение призрака к цели.
     * Для банковских бросков — двухфазное движение: сначала к стенке, потом к кольцу.
     */
    private void moveGhost(float dt) {
        float effTargetX = targetX;
        float effTargetY = targetY;

        if (currentShotType != SHOT_DIRECT && bankPhaseOne) {
            // Фаза 1: двигаемся к точке у стенки
            effTargetX = bankWallX;
            effTargetY = SCREEN_H * 0.45f;  // mid-height

            float dxW = bankWallX - ghostX;
            float dyW = effTargetY - ghostY;
            if (dxW * dxW + dyW * dyW < 60f * 60f) {
                // Достигли стенки — переходим к фазе 2 (движение к кольцу)
                bankPhaseOne = false;
            }
        }

        float dx = effTargetX - ghostX;
        float dy = effTargetY - ghostY;
        float dist2 = dx * dx + dy * dy;

        if (!bankPhaseOne && dist2 < 40f * 40f) {
            pickNewShot();
        }

        float ax = dx * 0.9f - vx * 1.8f;
        float ay = dy * 0.9f - vy * 1.8f;
        vx += ax * dt;
        vy += ay * dt;

        float maxV = 1100f;
        vx = clamp(vx, -maxV, maxV);
        vy = clamp(vy, -maxV, maxV);

        ghostX += vx * dt;
        ghostY += vy * dt;
        ghostX = clamp(ghostX, 0f, SCREEN_W);
        ghostY = clamp(ghostY, 0f, SCREEN_H);

        moving = (Math.abs(vx) + Math.abs(vy)) > 50f;
    }

    /**
     * Выполнить результат броска: попал или промахнулся.
     */
    private void executeShot(long now) {
        float pace = clamp(playerScorePerSec, 0f, 3.0f);

        // ── Шанс промаха ──────────────────────────────────────────────────
        // Базовый: 47 % (бот попадает максимум в ~65 % бросков)
        float missChance = 0.47f + 0.10f * scoreStreak - 0.05f * missStreak;

        // Банковские броски сложнее — шанс промаха выше
        if (currentShotType != SHOT_DIRECT) missChance += 0.12f;

        // «Холодная серия»: если счётчик активен, бот обязан промахнуться
        if (coldStreak > 0) {
            coldStreak--;
            missChance = 1.0f;
        }

        missChance = clamp(missChance, 0.35f, 0.88f);

        boolean miss = random.nextFloat() < missChance;

        if (miss) {
            missStreak++;
            scoreStreak = 0;
            // После 5+ промахов — иногда «разогреться»
            if (missStreak >= 5 && random.nextFloat() < 0.30f) {
                missStreak = 0;
            }
        } else {
            missStreak = 0;
            scoreStreak++;

            int delta = 1;
            // При хорошем темпе игрока иногда «двойной» бросок
            float twoChance = 0.06f + 0.05f * pace;
            if (random.nextFloat() < Math.min(0.20f, twoChance)) {
                delta = 2;
            }
            remoteScore += delta;

            // После 2+ попаданий подряд — активируем «холодную серию» с более высокой вероятностью
            if (scoreStreak >= 2 && random.nextFloat() < 0.55f) {
                coldStreak = 4 + random.nextInt(5);  // 4–8 обязательных промахов
                scoreStreak = 0;
            }
        }

        // ── Интервал до следующего броска ────────────────────────────────
        // Базовый интервал длиннее — бот не спамит броски
        long base = (long) (4500L - pace * 550L);

        int diff = lastPlayerScore - remoteScore;
        if (diff > 5) base -= 350L;   // отстаёт → немного ускоряется
        if (diff < -5) base += 400L;  // лидирует → немного медленнее

        if (miss) base += 600L + random.nextInt(700);  // после промаха — пауза

        base = (long) clamp(base, 1800L, 5500L);
        nextScoreAtMs = now + base + random.nextInt(600);

        // Выбрать тип следующего броска
        pickNewShot();
    }

    /**
     * Выбрать цель для следующего движения.
     * 28 % — банковский бросок от левой или правой стенки.
     */
    private void pickNewShot() {
        float r = random.nextFloat();
        if (r < 0.14f) {
            currentShotType = SHOT_BANK_LEFT;
            bankWallX = 40f + random.nextFloat() * 80f;   // левая стенка
            bankPhaseOne = true;
        } else if (r < 0.28f) {
            currentShotType = SHOT_BANK_RIGHT;
            bankWallX = SCREEN_W - 40f - random.nextFloat() * 80f;  // правая стенка
            bankPhaseOne = true;
        } else {
            currentShotType = SHOT_DIRECT;
            bankPhaseOne = false;
        }
        // Конечная цель — зона вокруг кольца (центр экрана, верхняя треть)
        targetX = SCREEN_W * 0.3f + random.nextFloat() * SCREEN_W * 0.4f;
        targetY = SCREEN_H * 0.25f + random.nextFloat() * SCREEN_H * 0.12f;
    }

    private void setTarget(float x, float y) {
        targetX = x;
        targetY = y;
    }

    private float jitter(float amp) {
        return (random.nextFloat() - 0.5f) * 2f * amp;
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(max, v));
    }
}

