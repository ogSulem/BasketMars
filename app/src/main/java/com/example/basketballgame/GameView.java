package com.example.basketballgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.MotionEvent;
import android.view.View;

import com.example.basketballgame.net.MatchClient;

import java.util.Locale;
import java.util.Random;

public class GameView extends View {
    public interface SessionListener {
        void onSessionComplete(GameMode mode, int playerScore, int rivalScore);
    }
    private GameMode gameMode = GameMode.ARCADE;
    private Bitmap ballBitmap;
    private Bitmap hoopBitmap;
    private final Bitmap[] ballPngBitmaps = new Bitmap[3];
    private final Paint ballPngPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix ballPngMatrix = new Matrix();
    private boolean usePngBall = true;
    private float ballX, ballY;
    private float ballRadius;
    private float hoopX, hoopY;
    private float hoopWidth, hoopHeight;
    private int score = 0;
    private float startX, startY, endX, endY;
    private boolean isMoving = false;
    private float velocityX, velocityY;
    private final float GRAVITY = 3.5f;
    private final float FRICTION = 0.96f;
    private Random random = new Random();
    private boolean scored = false;
    private boolean canSwipe = true;
    private boolean achievementUnlocked = false;
    private int selectedBall = 0; // 0 - обычный, 1 - новый
    private Bitmap altBallBitmap;
    private boolean showAchievement = false;
    private long achievementTime = 0;
    private boolean hitAnim = false;
    private long hitAnimStart = 0;
    private Bitmap ballBitmap3;
    private Bitmap hoopBitmap3;
    // Достижения
    private int highScore = 0;
    private int unlockedHoop = 0;
    private int unlockedBall = 0;
    private int achievementLevel = 0;
    private String achievementText = null;
    private long achievementShowTime = 0;
    private int selectedHoop = 0; // 0 - обычная, 1 - новая
    private Bitmap hoopBitmap2;
    private String achievementDesc = null;
    private int achievementIconRes = 0;

    private final Bitmap[] hoopPngBitmaps = new Bitmap[3];

    private final Bitmap[] hoopBodyMaskedBitmaps = new Bitmap[3];
    private int hoopBodyMaskedW = -1;
    private int hoopBodyMaskedH = -1;
    // Для усложнения
    private boolean showObstacle = false;
    private float obstacleX = 0, obstacleY = 0, obstacleW = 0, obstacleH = 0, obstacleVX = 0;
    private boolean showObstacle2 = false;
    private float obstacle2X = 0, obstacle2Y = 0, obstacle2VX = 0;
    private boolean movingHoop = false;
    private float hoopVX = 0;
    private int unlockedBg = 0;
    private int selectedBg = 0;
    private boolean showResetButton = false;
    private int comboStreak = 0;
    private int bestComboStreak = 0;
    private long comboShowUntilMs = 0L;
    // Paint кэш
    private final Paint scorePaint = new Paint();
    private final Paint achPaint = new Paint();
    private final Paint achBorderPaint = new Paint();
    private final Paint btnPaint = new Paint();
    private final Paint iconPaint = new Paint();
    private final Paint netPaint = new Paint();
    private final Paint hoopHitPaint = new Paint();
    private final Paint ballFlashPaint = new Paint();
    private final Paint obsPaint = new Paint();
    private final Paint obsPaint2 = new Paint();
    private final Paint flashPaint = new Paint();
    private final Paint rayPaint = new Paint();
    private final Paint timerPaint = new Paint();
    private final Paint hudBgPaint = new Paint();
    private final Paint ghostPaint = new Paint();
    private final Paint comboPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint comboGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint impactFlashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // Кэш Bitmap
    private Bitmap cachedBallBitmap = null;
    private float cachedBallRadius = -1;
    private Bitmap cachedHoopBitmap = null;
    private float cachedHoopWidth = -1, cachedHoopHeight = -1;

    private final Random fxRandom = new Random();
    private long shakeUntilMs = 0L;
    private float shakeAmpPx = 0f;
    private long rimShakeUntilMs = 0L;
    private float rimShakeAmpPx = 0f;
    private long rimShakeStartMs = 0L;
    private long impactFlashUntilMs = 0L;

    private long onboardingUntilMs = 0L;
    private String onboardingText = null;

    // Параметры для анимации сетки (волна по высоте)
    private static final int NET_NODES = 7;
    private final float[] netDx = new float[NET_NODES];
    private final float[] netVx = new float[NET_NODES];
    private final float[] netAx = new float[NET_NODES];
    private final float[] netDy = new float[NET_NODES];
    private final float[] netVy = new float[NET_NODES];
    private long lastNetUpdate = 0L;

    private long lastNetBallImpulseAtMs = 0L;

    // Режимы
    private boolean timedMode = false;
    private boolean onlineMode = false;
    private long modeDurationMs = 60_000;
    private long modeStartMs = 0L;
    private long remainingTimeMs = 60_000;
    private boolean gameOver = false;
    private int remoteScore = 0;
    private final GhostState ghostState = new GhostState();
    private SessionListener sessionListener;
    private MatchClient matchClient;

    private boolean paused = false;

    // Для дуэли с ботом: не стартовать соперника до первого броска игрока.
    private boolean deferOpponentStart = false;
    private boolean opponentStarted = false;
    private String opponentConnectTarget = null;

    private int remoteComboStreak = 0;
    private int lastRemoteScore = 0;
    private long remoteComboShowUntilMs = 0L;

    private boolean botShotActive = false;
    private long botShotStartMs = 0L;
    private long botShotDurMs = 520L;
    private float botFromX, botFromY;
    private float botToX, botToY;
    private float botPeakY;
    private float botBallX, botBallY;
    private float botSwipeX1, botSwipeY1, botSwipeX2, botSwipeY2;
    private long botSwipeUntilMs = 0L;

    private boolean botShotPlanned = false;
    private long botShotPlanAtMs = 0L;
    private int botShotPlannedDelta = 0;

    private boolean botPendingHoopMoveOnScore = false;

    private boolean enteredHoop = false;
    private float rimYTop = 0f;
    private float rimYBottom = 0f;
    private float rimInnerLeft = 0f;
    private float rimInnerRight = 0f;
    private float rimRadius = 0f;
    private float rimLeftX = 0f;
    private float rimRightX = 0f;
    private RectF backboardRect = new RectF();
    private final Paint rimFrontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ballShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long lastSnapshotSent = 0L;

    private float rimDrawX = 0f;
    private float rimDrawY = 0f;
    private float rimDrawW = 0f;
    private float rimDrawH = 0f;

    // Баланс сложности (Arcade)
    private float baseHoopVX = 0f;
    private float baseObstacleVX = 0f;
    private float baseObstacle2VX = 0f;

    private final Paint ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ballLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float rimW = 0f;

    private final Paint rimFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint netLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint rimHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimInnerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ballHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GameView(Context context) {
        this(context, (GameMode) null);
    }

    public GameView(Context context, GameMode mode) {
        super(context);
        this.gameMode = mode == null ? GameMode.ARCADE : mode;
        init(context);
    }

    private void init(Context context) {
        ballLinePaint.setStyle(Paint.Style.STROKE);
        ballLinePaint.setStrokeCap(Paint.Cap.ROUND);
        ballLinePaint.setStrokeJoin(Paint.Join.ROUND);
        ballLinePaint.setColor(Color.WHITE);
        ballLinePaint.setAlpha(255);

        rimFillPaint.setStyle(Paint.Style.FILL);
        rimOutlinePaint.setStyle(Paint.Style.STROKE);
        rimOutlinePaint.setStrokeCap(Paint.Cap.ROUND);
        rimOutlinePaint.setStrokeJoin(Paint.Join.ROUND);
        rimOutlinePaint.setColor(Color.WHITE);
        rimOutlinePaint.setShadowLayer(8, 0, 4, 0xAA000000);

        netLinePaint.setStyle(Paint.Style.STROKE);
        netLinePaint.setStrokeCap(Paint.Cap.ROUND);
        netLinePaint.setStrokeJoin(Paint.Join.ROUND);
        netLinePaint.setColor(0xFFFFFFFF);
        netLinePaint.setAlpha(175);

        rimHighlightPaint.setStyle(Paint.Style.STROKE);
        rimHighlightPaint.setStrokeCap(Paint.Cap.ROUND);
        rimHighlightPaint.setStrokeJoin(Paint.Join.ROUND);
        rimHighlightPaint.setColor(0x66FFFFFF);

        rimInnerShadowPaint.setStyle(Paint.Style.FILL);
        rimInnerShadowPaint.setColor(0x22000000);

        ballHighlightPaint.setStyle(Paint.Style.FILL);
        // Paint init
        scorePaint.setTextSize(140);
        scorePaint.setColor(0xFFFFFFFF);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setFakeBoldText(true);
        scorePaint.setShadowLayer(16, 0, 8, 0xFF8f5cff);
        scorePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        scorePaint.setStrokeWidth(6);
        achPaint.setTextSize(54);
        achPaint.setColor(0xFFFFFFFF);
        achPaint.setTextAlign(Paint.Align.CENTER);
        achPaint.setFakeBoldText(true);
        achPaint.setShadowLayer(16, 0, 8, 0xFF8f5cff);
        achBorderPaint.setColor(0xFFFFD700);
        achBorderPaint.setStyle(Paint.Style.STROKE);
        achBorderPaint.setStrokeWidth(12);
        btnPaint.setColor(0xFF8f5cff);
        btnPaint.setShadowLayer(24, 0, 12, 0x88000000);
        iconPaint.setColor(0xFFFFFFFF);
        iconPaint.setStrokeWidth(13);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        netPaint.setColor(0x99FFFFFF);
        netPaint.setStrokeWidth(6);
        hoopHitPaint.setColor(0x99FFD700);
        ballFlashPaint.setColor(0x55FFD700);
        obsPaint.setColor(0xFF8f5cff);
        obsPaint.setShadowLayer(16, 0, 8, 0xFF000000);
        obsPaint2.setColor(0xFFB266FF);
        obsPaint2.setShadowLayer(16, 0, 8, 0xFF000000);
        flashPaint.setColor(0xFFFFFFFF);
        rayPaint.setColor(0xFFFFFFFF);
        rayPaint.setStrokeWidth(13);
        rayPaint.setStyle(Paint.Style.STROKE);
        timerPaint.setColor(0xFFFFFFFF);
        timerPaint.setTextSize(64);
        timerPaint.setTextAlign(Paint.Align.CENTER);
        timerPaint.setFakeBoldText(true);
        timerPaint.setShadowLayer(12, 0, 6, 0xFF000000);
        hudBgPaint.setColor(0x44000000);
        hudBgPaint.setStyle(Paint.Style.FILL);
        hudBgPaint.setShadowLayer(24, 0, 16, 0xAA000000);
        ghostPaint.setFilterBitmap(true);

        comboPaint.setColor(0xFFFFEE58);
        comboPaint.setTextSize(64);
        comboPaint.setTextAlign(Paint.Align.CENTER);
        comboPaint.setFakeBoldText(true);
        comboPaint.setShadowLayer(12, 0, 6, 0xFF000000);

        rimFrontPaint.setStyle(Paint.Style.STROKE);
        rimFrontPaint.setStrokeCap(Paint.Cap.ROUND);
        rimFrontPaint.setColor(0xFFFFFFFF);
        rimFrontPaint.setShadowLayer(10, 0, 6, 0xAA000000);

        ballShadowPaint.setStyle(Paint.Style.FILL);
        ballShadowPaint.setColor(0x66000000);
        ghostPaint.setAntiAlias(true);
        // Читаем выбор мяча, корзины, фона и достижения
        SharedPreferences prefs = context.getSharedPreferences("basketball", Context.MODE_PRIVATE);
        selectedBall = prefs.getInt("selectedBall", 0);
        selectedHoop = prefs.getInt("selectedHoop", 0);
        if (selectedHoop < 0) selectedHoop = 0;
        if (selectedHoop > 2) selectedHoop = selectedHoop % 3;
        unlockedBall = prefs.getInt("unlockedBall", 0);
        unlockedHoop = prefs.getInt("unlockedHoop", 0);
        achievementLevel = prefs.getInt("achievementLevel", 0);
        achievementUnlocked = prefs.getBoolean("achievementUnlocked", false);
        unlockedBg = prefs.getInt("unlockedBg", 0);
        selectedBg = prefs.getInt("selectedBg", 0);
        // Установить фон
        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        setBackgroundResource(bgDrawables[selectedBg]);

        initModeState();

        maybeInitOnboarding();
    }

    private void maybeInitOnboarding() {
        SharedPreferences prefs = getContext().getSharedPreferences("basketball", Context.MODE_PRIVATE);
        String key = "onboarding_" + gameMode.name();
        boolean shown = prefs.getBoolean(key, false);
        if (shown) return;

        if (gameMode == GameMode.ARCADE) {
            onboardingText = "Аркада: набирай очки, сложность растёт";
        } else if (gameMode == GameMode.TIMED) {
            onboardingText = "На время: 60 сек. Комбо даёт бонус к очкам";
        } else {
            onboardingText = "Дуэль: обгони соперника за 60 сек";
        }

        onboardingUntilMs = System.currentTimeMillis() + 2300L;
        prefs.edit().putBoolean(key, true).apply();
    }

    private int getComboTierColor(int combo) {
        if (combo >= 8) return 0xFFE040FB; // красно-фиолетовый
        if (combo >= 5) return 0xFFFF9800; // оранжевый
        return 0xFFFFEE58; // жёлтый
    }

    private void drawProceduralBall(Canvas canvas, float x, float y, float radius) {
        if (usePngBall && renderBallFromPng(canvas, selectedBall, x, y, radius)) {
            return;
        }
        renderBallLikePng(canvas, selectedBall, x, y, radius);
    }

    private boolean renderBallFromPng(Canvas canvas, int ballSkin, float x, float y, float radius) {
        int resId;
        if (ballSkin == 1) resId = R.drawable.ball2;
        else if (ballSkin >= 2) resId = R.drawable.ball3;
        else resId = R.drawable.ball;

        Bitmap src = ballPngBitmaps[Math.max(0, Math.min(2, ballSkin))];
        if (src == null || src.isRecycled()) {
            try {
                src = BitmapFactory.decodeResource(getResources(), resId);
                ballPngBitmaps[Math.max(0, Math.min(2, ballSkin))] = src;
            } catch (Exception ignored) {
                src = null;
            }
        }
        if (src == null) return false;

        BitmapShader shader = new BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = (radius * 2f) / Math.min(src.getWidth(), src.getHeight());
        ballPngMatrix.reset();
        ballPngMatrix.postScale(scale, scale);
        float dx = x - (src.getWidth() * scale) / 2f;
        float dy = y - (src.getHeight() * scale) / 2f;
        ballPngMatrix.postTranslate(dx, dy);
        shader.setLocalMatrix(ballPngMatrix);
        ballPngPaint.setShader(shader);

        canvas.save();
        Path clip = new Path();
        clip.addCircle(x, y, radius, Path.Direction.CW);
        canvas.clipPath(clip);
        canvas.drawCircle(x, y, radius, ballPngPaint);
        canvas.restore();
        ballPngPaint.setShader(null);

        // тонкая обводка как “анти-лесенка”
        ballLinePaint.setStrokeWidth(radius * 0.045f);
        ballLinePaint.setColor(0xCCFFFFFF);
        ballLinePaint.setAlpha(170);
        canvas.drawCircle(x, y, radius * 0.995f, ballLinePaint);
        return true;
    }

    private void renderBallLikePng(Canvas canvas, int ballSkin, float x, float y, float radius) {
        int fill;
        int seam;
        
        if (ballSkin == 1) {
            fill = 0xFF1A1A1A;
            seam = Color.WHITE;
        } else if (ballSkin >= 2) {
            fill = 0xFF1A1A1A;
            seam = 0xFFB266FF;
        } else {
            fill = 0xFFE67E22; // Качественный оранжевый
            seam = 0xFF2C3E50; // Темные швы для классики
        }

        // Тело мяча с мягким объемом
        ballPaint.setStyle(Paint.Style.FILL);
        int light = blend(fill, Color.WHITE, 0.35f);
        int dark = blend(fill, Color.BLACK, 0.4f);
        ballPaint.setShader(new android.graphics.RadialGradient(x - radius * 0.35f, y - radius * 0.35f, radius * 1.6f,
                new int[]{light, fill, dark}, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius, ballPaint);
        ballPaint.setShader(null);

        // Блик для эффекта кожи/резины
        ballHighlightPaint.setColor(0x33FFFFFF);
        canvas.drawCircle(x - radius * 0.35f, y - radius * 0.35f, radius * 0.4f, ballHighlightPaint);

        // Реалистичные швы
        ballLinePaint.setStrokeWidth(radius * 0.085f);
        ballLinePaint.setColor(seam);
        ballLinePaint.setAlpha(ballSkin == 0 ? 210 : 185);

        // Клип по кругу — швы никогда не вылезут за контур
        canvas.save();
        Path clip = new Path();
        clip.addCircle(x, y, radius * 0.985f, Path.Direction.CW);
        canvas.clipPath(clip);

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(-25f);

        // Классические баскетбольные швы (дуги), без "клубка"
        float r = radius;

        // Две боковые дуги
        RectF left = new RectF(-r * 1.35f, -r * 1.15f, -r * 0.10f, r * 1.15f);
        RectF right = new RectF(r * 0.10f, -r * 1.15f, r * 1.35f, r * 1.15f);
        canvas.drawArc(left, 290, 140, false, ballLinePaint);
        canvas.drawArc(right, 110, 140, false, ballLinePaint);

        // Две верх/низ дуги
        RectF top = new RectF(-r * 1.05f, -r * 1.35f, r * 1.05f, -r * 0.05f);
        RectF bottom = new RectF(-r * 1.05f, r * 0.05f, r * 1.05f, r * 1.35f);
        canvas.drawArc(top, 20, 140, false, ballLinePaint);
        canvas.drawArc(bottom, 200, 140, false, ballLinePaint);
        
        canvas.restore();

        // выходим из клипа
        canvas.restore();

        // Тонкая обводка
        ballLinePaint.setStrokeWidth(radius * 0.05f);
        ballLinePaint.setAlpha(255);
        canvas.drawCircle(x, y, radius, ballLinePaint);
    }

    private int blend(int c1, int c2, float t) {
        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = (c1) & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF;
        int r2 = (c2 >>> 16) & 0xFF;
        int g2 = (c2 >>> 8) & 0xFF;
        int b2 = (c2) & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Bitmap renderBallPreview(Context context, int ballSkin, int sizePx) {
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint seam = new Paint(Paint.ANTI_ALIAS_FLAG);
        seam.setStyle(Paint.Style.STROKE);
        seam.setStrokeCap(Paint.Cap.ROUND);
        seam.setStrokeJoin(Paint.Join.ROUND);

        int fillCol;
        int seamCol;
        int borderCol;
        if (ballSkin == 1) {
            fillCol = 0xFF000000;
            seamCol = 0xFFFFFFFF;
            borderCol = 0xFFFFFFFF;
        } else if (ballSkin >= 2) {
            fillCol = 0xFF000000;
            seamCol = 0xFF5E1DBB;
            borderCol = 0xFF5E1DBB;
        } else {
            fillCol = 0xFFB07800;
            seamCol = 0xFFFFFFFF;
            borderCol = 0xFFFFFFFF;
        }

        float r = sizePx * 0.44f;
        float cx = sizePx / 2f;
        float cy = sizePx / 2f;

        // PNG-версия (вырез по кругу) — максимально “как в жизни”
        int resId;
        if (ballSkin == 1) resId = R.drawable.ball2;
        else if (ballSkin >= 2) resId = R.drawable.ball3;
        else resId = R.drawable.ball;
        Bitmap src = null;
        try {
            src = BitmapFactory.decodeResource(context.getResources(), resId);
        } catch (Exception ignored) {
        }
        if (src != null) {
            BitmapShader shader = new BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Matrix m = new Matrix();
            float scale = (r * 2f) / Math.min(src.getWidth(), src.getHeight());
            m.postScale(scale, scale);
            float dx = cx - (src.getWidth() * scale) / 2f;
            float dy = cy - (src.getHeight() * scale) / 2f;
            m.postTranslate(dx, dy);
            shader.setLocalMatrix(m);
            fill.setShader(shader);
            Path clip = new Path();
            clip.addCircle(cx, cy, r, Path.Direction.CW);
            c.save();
            c.clipPath(clip);
            c.drawCircle(cx, cy, r, fill);
            c.restore();
            fill.setShader(null);

            seam.setColor(0xCCFFFFFF);
            seam.setStrokeWidth(Math.max(2f, r * 0.05f));
            seam.setAlpha(170);
            c.drawCircle(cx, cy, r * 0.995f, seam);
            return bmp;
        }

        // Объём как в игре
        int light = blendStatic(fillCol, Color.WHITE, 0.35f);
        int dark = blendStatic(fillCol, Color.BLACK, 0.40f);
        fill.setStyle(Paint.Style.FILL);
        fill.setShader(new android.graphics.RadialGradient(cx - r * 0.35f, cy - r * 0.35f, r * 1.6f,
                new int[]{light, fillCol, dark}, new float[]{0f, 0.5f, 1f}, android.graphics.Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, fill);
        fill.setShader(null);

        // Клип по кругу — швы не вылезают
        Path clip = new Path();
        clip.addCircle(cx, cy, r * 0.985f, Path.Direction.CW);
        c.save();
        c.clipPath(clip);

        seam.setColor(seamCol);
        seam.setStrokeWidth(Math.max(4f, r * 0.085f));
        seam.setAlpha(ballSkin == 0 ? 210 : 185);

        c.save();
        c.translate(cx, cy);
        c.rotate(-25f);

        RectF left = new RectF(-r * 1.35f, -r * 1.15f, -r * 0.10f, r * 1.15f);
        RectF right = new RectF(r * 0.10f, -r * 1.15f, r * 1.35f, r * 1.15f);
        c.drawArc(left, 290, 140, false, seam);
        c.drawArc(right, 110, 140, false, seam);

        RectF top = new RectF(-r * 1.05f, -r * 1.35f, r * 1.05f, -r * 0.05f);
        RectF bottom = new RectF(-r * 1.05f, r * 0.05f, r * 1.05f, r * 1.35f);
        c.drawArc(top, 20, 140, false, seam);
        c.drawArc(bottom, 200, 140, false, seam);

        c.restore();
        c.restore();

        seam.setColor(borderCol);
        seam.setStrokeWidth(Math.max(3f, r * 0.05f));
        seam.setAlpha(255);
        c.drawCircle(cx, cy, r, seam);
        return bmp;
    }

    private static int blendStatic(int c1, int c2, float t) {
        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = (c1) & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF;
        int r2 = (c2 >>> 16) & 0xFF;
        int g2 = (c2 >>> 8) & 0xFF;
        int b2 = (c2) & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Bitmap renderHoopPreview(Context context, int hoopSkin, int sizePx) {
        Bitmap bmp;
        try {
            bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        Canvas c = new Canvas(bmp);

        float w = sizePx;
        float h = sizePx;

        int idx = hoopSkin;
        if (idx < 0) idx = 0;
        if (idx > 2) idx = idx % 3;

        float hx = w * 0.08f;
        float hy = h * 0.08f;
        float hw = w * 0.84f;
        float hh = h * 0.84f;

        float rimH = hh * 0.10f;
        float rimW = hw * 0.92f;
        float rimX = hx + (hw - rimW) / 2f;
        float rimY = hy + hh * 0.14f;

        RectF rimRect = new RectF(rimX, rimY, rimX + rimW, rimY + rimH);
        Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setStyle(Paint.Style.FILL);
        Paint rimStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        rimStroke.setStyle(Paint.Style.STROKE);
        rimStroke.setStrokeJoin(Paint.Join.ROUND);
        rimStroke.setStrokeCap(Paint.Cap.ROUND);
        rimStroke.setStrokeWidth(Math.max(2.0f, rimH * 0.12f));

        int netCol = 0xFFFFFFFF;
        if (idx == 2) {
            rimPaint.setColor(0xFFF2C200);
            rimStroke.setColor(0xFF6A00FF);
        } else if (idx == 1) {
            rimPaint.setColor(0xFF0B0B0B);
            rimStroke.setColor(0xFF3D7BFF);
        } else {
            rimPaint.setColor(0xFFFFFFFF);
            rimStroke.setColor(0xFF0B0B0B);
        }

        c.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimPaint);
        c.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimStroke);

        if (idx == 1) {
            Paint marsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            marsPaint.setColor(0xFFFFFFFF);
            marsPaint.setTextAlign(Paint.Align.CENTER);
            marsPaint.setFakeBoldText(true);
            marsPaint.setTextSize(Math.max(10f, rimH * 0.55f));
            float textY = rimRect.centerY() + marsPaint.getTextSize() * 0.32f;
            c.drawText("MARS", rimRect.centerX(), textY, marsPaint);
        }

        float topNetY = rimY + rimH * 0.88f;
        float bottomNetY = hy + hh * 0.82f;
        float topL = rimX + rimW * 0.08f;
        float topR = rimX + rimW * 0.92f;
        float botL = hx + hw * 0.28f;
        float botR = hx + hw * 0.72f;

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        stroke.setColor(idx == 2 ? 0xFF6A00FF : netCol);
        stroke.setAlpha(idx == 2 ? 235 : 220);
        stroke.setStrokeWidth(Math.max(6.0f, hh * 0.026f));

        int cols = 4;
        int rows = 5;
        float[][] px = new float[rows + 1][cols + 1];
        float[][] py = new float[rows + 1][cols + 1];

        for (int rr0 = 0; rr0 <= rows; rr0++) {
            float rt = rr0 / (float) rows;
            float y = lerpStatic(topNetY, bottomNetY, rt);
            float xL = lerpStatic(topL, botL, rt);
            float xR = lerpStatic(topR, botR, rt);
            for (int cc0 = 0; cc0 <= cols; cc0++) {
                float ct = cc0 / (float) cols;
                px[rr0][cc0] = lerpStatic(xL, xR, ct);
                py[rr0][cc0] = y;
            }
        }

        for (int rr0 = 0; rr0 < rows; rr0++) {
            for (int cc0 = 0; cc0 < cols; cc0++) {
                c.drawLine(px[rr0][cc0], py[rr0][cc0], px[rr0 + 1][cc0 + 1], py[rr0 + 1][cc0 + 1], stroke);
                c.drawLine(px[rr0][cc0 + 1], py[rr0][cc0 + 1], px[rr0 + 1][cc0], py[rr0 + 1][cc0], stroke);
            }
        }

        c.drawLine(px[rows][0], py[rows][0], px[rows][cols], py[rows][cols], stroke);

        return bmp;
    }

    private static float lerpStatic(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private void drawProceduralHoop(Canvas canvas, boolean isBackLayer) {
        updateNetPhysics();
        drawHoopLikePng(canvas, selectedHoop, hoopX, hoopY, hoopWidth, hoopHeight, isBackLayer ? 0f : 1f);
    }

    private void drawHoopLikePng(Canvas canvas, int hoopSkin, float hx, float hy, float hw, float hh, float layer) {
        if (hoopSkin == 0) {
            drawHoopLikePngClassic(canvas, hx, hy, hw, hh, layer);
            return;
        }
        if (hoopSkin == 1) {
            drawHoopLikePngMarsel(canvas, hx, hy, hw, hh, layer);
            return;
        }
        if (hoopSkin == 2) {
            drawHoopLikePngPurple(canvas, hx, hy, hw, hh, layer);
            return;
        }
        // Обод рисуем как капсулу с градиентом
        int rimFill;
        if (hoopSkin == 0) rimFill = 0xFFFF8A00;
        else if (hoopSkin == 1) rimFill = 0xFFFFFFFF;
        else rimFill = 0xFFFFC107;

        float rimH = hh * 0.10f;
        float rimW = hw * 0.92f;
        float rimX = hx + (hw - rimW) / 2f;
        float rimY = hy + hh * 0.14f;
        float rr = rimH * 0.5f;

        float outlineW = Math.max(4f, rimH * 0.15f);

        RectF rimRect = new RectF(rimX, rimY, rimX + rimW, rimY + rimH);
        float holeInset = Math.max(6f, outlineW * 0.95f);
        RectF holeRect = new RectF(rimRect.left + holeInset, rimRect.top + holeInset * 0.65f,
                rimRect.right - holeInset, rimRect.bottom - holeInset * 0.65f);

        if (layer >= 1f) {
            // Передняя часть обода — более реалистичный «тор» (овал с отверстием)
            Paint outerShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
            outerShadow.setStyle(Paint.Style.FILL);
            outerShadow.setColor(0x33000000);
            float shPad = Math.max(2f, rimH * 0.18f);
            RectF sh = new RectF(rimRect.left - shPad, rimRect.top - shPad * 0.65f, rimRect.right + shPad, rimRect.bottom + shPad * 0.85f);
            canvas.drawOval(sh, outerShadow);

            int topCol = blend(rimFill, Color.WHITE, 0.40f);
            int midCol = rimFill;
            int botCol = blend(rimFill, Color.BLACK, 0.30f);
            rimFillPaint.setShader(new LinearGradient(rimRect.left, rimRect.top, rimRect.left, rimRect.bottom,
                    new int[]{topCol, midCol, botCol}, new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));

            Path ring = new Path();
            ring.setFillType(Path.FillType.EVEN_ODD);
            ring.addOval(rimRect, Path.Direction.CW);
            ring.addOval(holeRect, Path.Direction.CW);
            canvas.drawPath(ring, rimFillPaint);

            // Внутренняя тень «дырки»
            Paint innerShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
            innerShadow.setStyle(Paint.Style.STROKE);
            innerShadow.setStrokeWidth(Math.max(4f, outlineW * 0.55f));
            innerShadow.setColor(0x66000000);
            canvas.drawOval(holeRect, innerShadow);

            // Внешний контур
            rimOutlinePaint.setStrokeWidth(outlineW);
            rimOutlinePaint.setColor(Color.WHITE);
            canvas.drawOval(rimRect, rimOutlinePaint);

            // Блик сверху (дугой)
            rimHighlightPaint.setStrokeWidth(Math.max(3f, outlineW * 0.55f));
            rimHighlightPaint.setColor(0x66FFFFFF);
            RectF hi = new RectF(rimRect.left + rimRect.width() * 0.06f, rimRect.top + rimRect.height() * 0.06f,
                    rimRect.right - rimRect.width() * 0.06f, rimRect.bottom);
            canvas.drawArc(hi, 200, 140, false, rimHighlightPaint);

            Paint bottomGlow = new Paint(Paint.ANTI_ALIAS_FLAG);
            bottomGlow.setStyle(Paint.Style.STROKE);
            bottomGlow.setStrokeCap(Paint.Cap.ROUND);
            bottomGlow.setStrokeWidth(Math.max(2f, outlineW * 0.45f));
            bottomGlow.setColor(0x22FFFFFF);
            RectF lo = new RectF(rimRect.left + rimRect.width() * 0.10f, rimRect.top, rimRect.right - rimRect.width() * 0.10f, rimRect.bottom - rimRect.height() * 0.08f);
            canvas.drawArc(lo, 20, 140, false, bottomGlow);

            rimFillPaint.setShader(null);
            return;
        }

        // Задняя часть обода — темнее и проще
        Paint backRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backRimPaint.setStyle(Paint.Style.FILL);
        backRimPaint.setColor(blend(rimFill, Color.BLACK, 0.55f));
        Path backRing = new Path();
        backRing.setFillType(Path.FillType.EVEN_ODD);
        backRing.addOval(rimRect, Path.Direction.CW);
        backRing.addOval(holeRect, Path.Direction.CW);
        canvas.drawPath(backRing, backRimPaint);

        // Реалистичная физическая сетка (дуги)
        float topNetY = rimY + rimH * 0.85f;
        float bottomNetY = hy + hh * 0.80f;
        float topL = rimX + rimW * 0.10f;
        float topR = rimX + rimW * 0.90f;
        float botL = hx + hw * 0.28f;
        float botR = hx + hw * 0.72f;

        float netDxScale = hw * 0.045f;
        float netDyScale = hh * 0.050f;

        netLinePaint.setStrokeWidth(Math.max(4.6f, hh * 0.0205f));
        netLinePaint.setColor(Color.WHITE);
        netLinePaint.setAlpha(160);

        int cols = 8;
        for (int i = 0; i <= cols; i++) {
            float t = (float) i / cols;
            float xTop = lerp(topL, topR, t);
            // Каждый узел сетки имеет свое отклонение
            float nodeIdx = t * (NET_NODES - 1);
            int ni = (int) nodeIdx;
            if (ni < 0) ni = 0;
            if (ni > NET_NODES - 1) ni = NET_NODES - 1;
            float dx = netDx[ni] * netDxScale;
            float dy = netDy[ni] * netDyScale;
            float xBot = lerp(botL, botR, t) + dx;
            float yBot = bottomNetY + dy;
            
            Path strand = new Path();
            strand.moveTo(xTop, topNetY);
            float cpX = lerp(xTop, xBot, 0.5f) + dx * 0.5f;
            float cpY = lerp(topNetY, yBot, 0.5f) + Math.abs(dy) * 0.35f;
            strand.quadTo(cpX, cpY, xBot, yBot);
            canvas.drawPath(strand, netLinePaint);
        }
        
        // Горизонтальные кольца сетки (кривые)
        int rows = 4;
        for (int j = 1; j <= rows; j++) {
            float rt = (float) j / (rows + 1);
            float nodeIdx = rt * (NET_NODES - 1);
            int ni = (int) nodeIdx;
            if (ni < 0) ni = 0;
            if (ni > NET_NODES - 1) ni = NET_NODES - 1;
            float dx = netDx[ni] * netDxScale;
            float dy = netDy[ni] * netDyScale * (0.65f + 0.35f * rt);
            float y = lerp(topNetY, bottomNetY, rt) + dy;
            float xL = lerp(topL, botL, rt) + dx;
            float xR = lerp(topR, botR, rt) + dx;
            
            Path ring = new Path();
            ring.moveTo(xL, y);
            ring.quadTo(lerp(xL, xR, 0.5f), y + hh * 0.02f, xR, y);
            canvas.drawPath(ring, netLinePaint);
        }
    }

    private void drawHoopLikePngPurple(Canvas canvas, float hx, float hy, float hw, float hh, float layer) {
        int rimFill = 0xFFF2C200;
        int outlineCol = 0xFF6A00FF;

        float rimH = hh * 0.10f;
        float rimW = hw * 0.92f;
        float rimX = hx + (hw - rimW) / 2f;
        float rimY = hy + hh * 0.14f;

        long now = System.currentTimeMillis();
        if (now < rimShakeUntilMs) {
            float t = (rimShakeUntilMs - now) / 220f;
            float k = Math.max(0f, Math.min(1f, t));
            float amp = k * rimShakeAmpPx;
            float ph = (now - rimShakeStartMs) * 0.090f;
            rimX += (float) Math.sin(ph) * amp;
            rimY += (float) Math.cos(ph * 1.12f) * amp * 0.55f;
        }

        if (layer >= 1f) {
            RectF rimRect = new RectF(rimX, rimY, rimX + rimW, rimY + rimH);

            Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rimPaint.setStyle(Paint.Style.FILL);
            rimPaint.setColor(rimFill);
            canvas.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimPaint);

            Paint rimStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            rimStroke.setStyle(Paint.Style.STROKE);
            rimStroke.setStrokeJoin(Paint.Join.ROUND);
            rimStroke.setStrokeCap(Paint.Cap.ROUND);
            rimStroke.setStrokeWidth(Math.max(2.0f, rimH * 0.12f));
            rimStroke.setColor(outlineCol);
            canvas.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimStroke);
            return;
        }

        float topNetY = rimY + rimH * 0.88f;
        float bottomNetY = hy + hh * 0.82f;
        float topL = rimX + rimW * 0.08f;
        float topR = rimX + rimW * 0.92f;
        float botL = hx + hw * 0.28f;
        float botR = hx + hw * 0.72f;

        float netDxScale = hw * 0.040f;
        float netDyScale = hh * 0.050f;

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        stroke.setColor(outlineCol);
        stroke.setAlpha(235);
        stroke.setStrokeWidth(Math.max(6.5f, hh * 0.028f));

        int cols = 4;
        int rows = 5;
        float[][] px = new float[rows + 1][cols + 1];
        float[][] py = new float[rows + 1][cols + 1];

        for (int rr = 0; rr <= rows; rr++) {
            float rt = rr / (float) rows;
            float nodeIdx = rt * (NET_NODES - 1);
            int ni = (int) nodeIdx;
            if (ni < 0) ni = 0;
            if (ni > NET_NODES - 1) ni = NET_NODES - 1;
            float dx = netDx[ni] * netDxScale;
            float dy = netDy[ni] * netDyScale * (0.55f + 0.45f * rt);

            float y = lerp(topNetY, bottomNetY, rt) + dy;
            float xL = lerp(topL, botL, rt) + dx;
            float xR = lerp(topR, botR, rt) + dx;
            for (int cc = 0; cc <= cols; cc++) {
                float ct = cc / (float) cols;
                px[rr][cc] = lerp(xL, xR, ct);
                py[rr][cc] = y;
            }
        }

        for (int rr = 0; rr < rows; rr++) {
            for (int cc = 0; cc < cols; cc++) {
                canvas.drawLine(px[rr][cc], py[rr][cc], px[rr + 1][cc + 1], py[rr + 1][cc + 1], stroke);
                canvas.drawLine(px[rr][cc + 1], py[rr][cc + 1], px[rr + 1][cc], py[rr + 1][cc], stroke);
            }
        }

        canvas.drawLine(px[rows][0], py[rows][0], px[rows][cols], py[rows][cols], stroke);
    }

    private void drawHoopRimFromPng(Canvas canvas, int hoopSkin, float hx, float hy, float hw, float hh) {
        Bitmap src = getHoopPngBitmap(hoopSkin);
        if (src == null || src.isRecycled()) return;

        int sw = src.getWidth();
        int sh = src.getHeight();
        if (sw <= 0 || sh <= 0) return;

        int rimBandTop = 0;
        int rimBandBottom = Math.max(1, (int) (sh * 0.26f));
        if (rimBandBottom > sh) rimBandBottom = sh;

        Rect srcRect = new Rect(0, rimBandTop, sw, rimBandBottom);
        RectF dstRect = new RectF(hx, hy, hx + hw, hy + hh * 0.26f);
        canvas.drawBitmap(src, srcRect, dstRect, null);
    }

    private void drawHoopBodyFromPngClearingNet(Canvas canvas, int hoopSkin, float hx, float hy, float hw, float hh) {
        Bitmap masked = getHoopBodyMaskedBitmap(hoopSkin, (int) Math.max(1, hw), (int) Math.max(1, hh));
        if (masked == null || masked.isRecycled()) return;
        canvas.drawBitmap(masked, hx, hy, null);
    }

    private Bitmap getHoopBodyMaskedBitmap(int hoopSkin, int w, int h) {
        int idx = Math.max(0, Math.min(2, hoopSkin));
        if (w != hoopBodyMaskedW || h != hoopBodyMaskedH) {
            for (int i = 0; i < hoopBodyMaskedBitmaps.length; i++) {
                if (hoopBodyMaskedBitmaps[i] != null && !hoopBodyMaskedBitmaps[i].isRecycled()) {
                    hoopBodyMaskedBitmaps[i].recycle();
                }
                hoopBodyMaskedBitmaps[i] = null;
            }
            hoopBodyMaskedW = w;
            hoopBodyMaskedH = h;
        }

        Bitmap cached = hoopBodyMaskedBitmaps[idx];
        if (cached != null && !cached.isRecycled()) return cached;

        Bitmap src = getHoopPngBitmap(idx);
        if (src == null || src.isRecycled()) return null;

        Bitmap out;
        try {
            out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }

        Canvas c = new Canvas(out);
        RectF dst = new RectF(0, 0, w, h);
        c.drawBitmap(src, null, dst, null);

        // Clear the static net inside the PNG so only our dynamic net remains.
        float topNetY = h * 0.08f;
        float bottomNetY = h * 0.90f;
        float topL = w * 0.02f;
        float topR = w * 0.98f;
        float botL = w * 0.22f;
        float botR = w * 0.78f;

        Path clip = new Path();
        clip.moveTo(topL, topNetY);
        clip.lineTo(topR, topNetY);
        clip.lineTo(botR, bottomNetY);
        clip.lineTo(botL, bottomNetY);
        clip.close();

        RectF circleKill = new RectF(w * 0.08f, h * 0.06f, w * 0.92f, h * 0.62f);
        clip.addOval(circleKill, Path.Direction.CW);

        Paint clear = new Paint(Paint.ANTI_ALIAS_FLAG);
        clear.setStyle(Paint.Style.FILL);
        clear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        c.drawPath(clip, clear);
        clear.setXfermode(null);

        hoopBodyMaskedBitmaps[idx] = out;
        return out;
    }

    private Bitmap getHoopPngBitmap(int hoopSkin) {
        int idx = Math.max(0, Math.min(2, hoopSkin));
        Bitmap cached = hoopPngBitmaps[idx];
        if (cached != null && !cached.isRecycled()) return cached;

        int resId;
        if (idx == 2) resId = R.drawable.hoop3;
        else if (idx == 1) resId = R.drawable.hoop2;
        else resId = R.drawable.hoop;

        try {
            Bitmap src = BitmapFactory.decodeResource(getResources(), resId);
            hoopPngBitmaps[idx] = src;
            return src;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void drawHoopLikePngClassic(Canvas canvas, float hx, float hy, float hw, float hh, float layer) {
        float rimH = hh * 0.10f;
        float rimW = hw * 0.92f;
        float rimX = hx + (hw - rimW) / 2f;
        float rimY = hy + hh * 0.14f;
        int netCol = 0xFFFFFFFF;

        long now = System.currentTimeMillis();
        if (now < rimShakeUntilMs) {
            float t = (rimShakeUntilMs - now) / 260f;
            float k = Math.max(0f, Math.min(1f, t));
            float amp = k * rimShakeAmpPx;
            float ph = (now - rimShakeStartMs) * 0.095f;
            rimX += (float) Math.sin(ph) * amp;
            rimY += (float) Math.cos(ph * 1.12f) * amp * 0.55f;
        }

        if (layer >= 1f) {
            RectF rimRect = new RectF(rimX, rimY, rimX + rimW, rimY + rimH);

            Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rimPaint.setStyle(Paint.Style.FILL);
            rimPaint.setColor(0xFFFFFFFF);
            canvas.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimPaint);

            Paint rimStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            rimStroke.setStyle(Paint.Style.STROKE);
            rimStroke.setStrokeJoin(Paint.Join.ROUND);
            rimStroke.setStrokeCap(Paint.Cap.ROUND);
            rimStroke.setStrokeWidth(Math.max(2.0f, rimH * 0.12f));
            rimStroke.setColor(0xFF3D7BFF);
            canvas.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimStroke);
            return;
        }

        float topNetY = rimY + rimH * 0.88f;
        float bottomNetY = hy + hh * 0.82f;
        float topL = rimX + rimW * 0.08f;
        float topR = rimX + rimW * 0.92f;
        float botL = hx + hw * 0.28f;
        float botR = hx + hw * 0.72f;

        float netDxScale = hw * 0.040f;
        float netDyScale = hh * 0.050f;

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        stroke.setColor(netCol);
        stroke.setAlpha(220);
        stroke.setStrokeWidth(Math.max(6.0f, hh * 0.026f));

        int cols = 4;
        int rows = 5;
        float[][] px = new float[rows + 1][cols + 1];
        float[][] py = new float[rows + 1][cols + 1];

        for (int rr0 = 0; rr0 <= rows; rr0++) {
            float rt = rr0 / (float) rows;

            float nodeIdx = rt * (NET_NODES - 1);
            int ni = (int) nodeIdx;
            if (ni < 0) ni = 0;
            if (ni > NET_NODES - 1) ni = NET_NODES - 1;
            float dx = netDx[ni] * netDxScale;
            float dy = netDy[ni] * netDyScale * (0.55f + 0.45f * rt);

            float y = lerp(topNetY, bottomNetY, rt) + dy;
            float xL = lerp(topL, botL, rt) + dx;
            float xR = lerp(topR, botR, rt) + dx;
            for (int cc0 = 0; cc0 <= cols; cc0++) {
                float ct = cc0 / (float) cols;
                px[rr0][cc0] = lerp(xL, xR, ct);
                py[rr0][cc0] = y;
            }
        }

        for (int rr0 = 0; rr0 < rows; rr0++) {
            for (int cc0 = 0; cc0 < cols; cc0++) {
                canvas.drawLine(px[rr0][cc0], py[rr0][cc0], px[rr0 + 1][cc0 + 1], py[rr0 + 1][cc0 + 1], stroke);
                canvas.drawLine(px[rr0][cc0 + 1], py[rr0][cc0 + 1], px[rr0 + 1][cc0], py[rr0 + 1][cc0], stroke);
            }
        }

        canvas.drawLine(px[rows][0], py[rows][0], px[rows][cols], py[rows][cols], stroke);
    }

    private void drawHoopLikePngMarsel(Canvas canvas, float hx, float hy, float hw, float hh, float layer) {
        float rimH = hh * 0.10f;
        float rimW = hw * 0.92f;
        float rimX = hx + (hw - rimW) / 2f;
        float rimY = hy + hh * 0.14f;
        int netCol = 0xFFFFFFFF;

        long now = System.currentTimeMillis();
        if (now < rimShakeUntilMs) {
            float t = (rimShakeUntilMs - now) / 260f;
            float k = Math.max(0f, Math.min(1f, t));
            float amp = k * rimShakeAmpPx;
            float ph = (now - rimShakeStartMs) * 0.095f;
            rimX += (float) Math.sin(ph) * amp;
            rimY += (float) Math.cos(ph * 1.12f) * amp * 0.55f;
        }

        if (layer >= 1f) {
            RectF rimRect = new RectF(rimX, rimY, rimX + rimW, rimY + rimH);

            Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rimPaint.setStyle(Paint.Style.FILL);
            rimPaint.setColor(0xFF0B0B0B);
            canvas.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimPaint);

            Paint rimStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            rimStroke.setStyle(Paint.Style.STROKE);
            rimStroke.setStrokeJoin(Paint.Join.ROUND);
            rimStroke.setStrokeCap(Paint.Cap.ROUND);
            rimStroke.setStrokeWidth(Math.max(2.0f, rimH * 0.12f));
            rimStroke.setColor(0xFF3D7BFF);
            canvas.drawRoundRect(rimRect, rimH * 0.5f, rimH * 0.5f, rimStroke);

            Paint marsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            marsPaint.setColor(0xFFFFFFFF);
            marsPaint.setTextAlign(Paint.Align.CENTER);
            marsPaint.setFakeBoldText(true);
            marsPaint.setTextSize(Math.max(10f, rimH * 0.62f));
            float textY = rimRect.centerY() + marsPaint.getTextSize() * 0.32f;
            canvas.drawText("MARS", rimRect.centerX(), textY, marsPaint);
            return;
        }

        float topNetY = rimY + rimH * 0.88f;
        float bottomNetY = hy + hh * 0.82f;
        float topL = rimX + rimW * 0.08f;
        float topR = rimX + rimW * 0.92f;
        float botL = hx + hw * 0.28f;
        float botR = hx + hw * 0.72f;

        float netDxScale = hw * 0.040f;
        float netDyScale = hh * 0.050f;

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        stroke.setColor(netCol);
        stroke.setAlpha(230);
        stroke.setStrokeWidth(Math.max(6.0f, hh * 0.026f));

        int cols = 4;
        int rows = 5;
        float[][] px = new float[rows + 1][cols + 1];
        float[][] py = new float[rows + 1][cols + 1];

        for (int rr0 = 0; rr0 <= rows; rr0++) {
            float rt = rr0 / (float) rows;

            float nodeIdx = rt * (NET_NODES - 1);
            int ni = (int) nodeIdx;
            if (ni < 0) ni = 0;
            if (ni > NET_NODES - 1) ni = NET_NODES - 1;
            float dx = netDx[ni] * netDxScale;
            float dy = netDy[ni] * netDyScale * (0.55f + 0.45f * rt);

            float y = lerp(topNetY, bottomNetY, rt) + dy;
            float xL = lerp(topL, botL, rt) + dx;
            float xR = lerp(topR, botR, rt) + dx;
            for (int cc0 = 0; cc0 <= cols; cc0++) {
                float ct = cc0 / (float) cols;
                px[rr0][cc0] = lerp(xL, xR, ct);
                py[rr0][cc0] = y;
            }
        }

        for (int rr0 = 0; rr0 < rows; rr0++) {
            for (int cc0 = 0; cc0 < cols; cc0++) {
                canvas.drawLine(px[rr0][cc0], py[rr0][cc0], px[rr0 + 1][cc0 + 1], py[rr0 + 1][cc0 + 1], stroke);
                canvas.drawLine(px[rr0][cc0 + 1], py[rr0][cc0 + 1], px[rr0 + 1][cc0], py[rr0 + 1][cc0], stroke);
            }
        }

        canvas.drawLine(px[rows][0], py[rows][0], px[rows][cols], py[rows][cols], stroke);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private void updateNetPhysics() {
        long now = System.currentTimeMillis();
        if (lastNetUpdate == 0) { lastNetUpdate = now; return; }
        float dt = (now - lastNetUpdate) / 1000f;
        if (dt <= 0f) dt = 0.016f;
        if (dt > 0.045f) dt = 0.045f;
        lastNetUpdate = now;

        float k = 92f;
        float neighbor = 56f;
        float dampingPerFrame = 0.90f;
        float damping = (float) Math.pow(dampingPerFrame, dt * 60f);

        float[] ax = new float[NET_NODES];
        for (int i = 0; i < NET_NODES; i++) {
            float a = -k * netDx[i];
            if (i > 0) a += neighbor * (netDx[i - 1] - netDx[i]);
            if (i < NET_NODES - 1) a += neighbor * (netDx[i + 1] - netDx[i]);
            ax[i] = a;
        }

        boolean active = false;
        for (int i = 0; i < NET_NODES; i++) {
            netVx[i] += ax[i] * dt;
            netVx[i] *= damping;
            netDx[i] += netVx[i] * dt;
            netDx[i] = clamp(netDx[i], -1.25f, 1.25f);
            netVx[i] = clamp(netVx[i], -12.0f, 12.0f);
            if (Math.abs(netDx[i]) > 0.08f || Math.abs(netVx[i]) > 0.08f) active = true;
        }

        float kY = 78f;
        float neighborY = 46f;
        float[] ay = new float[NET_NODES];
        for (int i = 0; i < NET_NODES; i++) {
            float a = -kY * netDy[i];
            if (i > 0) a += neighborY * (netDy[i - 1] - netDy[i]);
            if (i < NET_NODES - 1) a += neighborY * (netDy[i + 1] - netDy[i]);
            ay[i] = a;
        }
        for (int i = 0; i < NET_NODES; i++) {
            netVy[i] += ay[i] * dt;
            netVy[i] *= damping;
            netDy[i] += netVy[i] * dt;
            netDy[i] = clamp(netDy[i], -1.6f, 1.6f);
            netVy[i] = clamp(netVy[i], -14.0f, 14.0f);
            if (Math.abs(netDy[i]) > 0.06f || Math.abs(netVy[i]) > 0.06f) active = true;
        }

        if (active) postInvalidate();
    }

    private int getRimColorForHoopSkin() {
        if (selectedHoop == 0) return 0xFFFF9800;
        if (selectedHoop == 1) return 0xFF0B0B0B;
        return 0xFFFFC107;
    }

    private void initModeState() {
        switch (gameMode) {
            case TIMED:
                timedMode = true;
                onlineMode = false;
                modeDurationMs = 60_000; // 60 seconds
                break;
            case ONLINE_DUEL:
                timedMode = true;
                onlineMode = true;
                modeDurationMs = 60_000; // 60 seconds
                break;
            case ARCADE:
            default:
                timedMode = false;
                onlineMode = false;
                modeDurationMs = 60_000; // default, not used in arcade
                break;
        }
        modeStartMs = System.currentTimeMillis();
        remainingTimeMs = modeDurationMs;
        gameOver = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ballRadius = w / 8f * 0.85f;
        ballX = w / 2f;
        ballY = h - ballRadius * 2;
        float aspect = (float) 1.5;
        hoopWidth = w / 2.5f * 0.85f;
        hoopHeight = hoopWidth * aspect;
        // Кэшируем Bitmap мяча
        cachedBallBitmap = null;
        cachedHoopBitmap = null;
        cachedBallRadius = -1;
        cachedHoopWidth = -1;
        cachedHoopHeight = -1;
        spawnHoop(w, h);
        computeHoopGeometry();
        obstacleW = w / 4f;
        obstacleH = h / 30f;
        float forbiddenLeft = hoopX - obstacleW - 40;
        float forbiddenRight = hoopX + hoopWidth + 40;
        // Препятствие 1
        obstacleY = h / 2.2f;
        do {
            obstacleX = (float)(Math.random() * (w - obstacleW));
        } while (obstacleX + obstacleW > forbiddenLeft && obstacleX < forbiddenRight);
        obstacleVX = w / 180f;
        // Препятствие 2
        obstacle2Y = h / 3f;
        do {
            obstacle2X = (float)(Math.random() * (w - obstacleW));
        } while (obstacle2X + obstacleW > forbiddenLeft && obstacle2X < forbiddenRight);
        obstacle2VX = -w / 160f;
        // Кольцо
        hoopVX = w / 180f;

        baseHoopVX = hoopVX;
        baseObstacleVX = obstacleVX;
        baseObstacle2VX = obstacle2VX;

        applyArcadeDifficulty();

        ensureObstaclesNotBlockingHoop(w, h);
    }

    private void applyArcadeDifficulty() {
        if (getWidth() <= 0) return;
        if (gameMode != GameMode.ARCADE) return;

        // Плавная кривая сложности: 0..1 при 0..30 очков
        float t = Math.max(0f, Math.min(1f, score / 30f));

        // Когда включаются элементы сложности
        showObstacle = score >= 3;
        movingHoop = score >= 6;
        showObstacle2 = score >= 10;

        // Ускоряем движение (сохраняем направление)
        float hoopMul = 1.0f + 1.05f * t;
        float obsMul = 1.0f + 1.20f * t;
        float obs2Mul = 1.0f + 1.25f * t;

        if (baseHoopVX != 0f) {
            hoopVX = Math.signum(hoopVX == 0f ? baseHoopVX : hoopVX) * Math.abs(baseHoopVX) * hoopMul;
        }
        if (baseObstacleVX != 0f) {
            obstacleVX = Math.signum(obstacleVX == 0f ? baseObstacleVX : obstacleVX) * Math.abs(baseObstacleVX) * obsMul;
        }
        if (baseObstacle2VX != 0f) {
            obstacle2VX = Math.signum(obstacle2VX == 0f ? baseObstacle2VX : obstacle2VX) * Math.abs(baseObstacle2VX) * obs2Mul;
        }
    }

    private void computeHoopGeometry() {
        // СИНХРОН с процедурной отрисовкой (обод-капсула как hoop.png)
        rimDrawH = hoopHeight * 0.10f;
        rimDrawW = hoopWidth * 0.92f;
        rimDrawX = hoopX + (hoopWidth - rimDrawW) / 2f;
        rimDrawY = hoopY + hoopHeight * 0.14f;

        // Плоскость обода для коллизии/засчёта (примерно середина капсулы)
        rimYTop = rimDrawY + rimDrawH * 0.55f;
        // Нижняя плоскость для "честного" прохода (немного ниже обода)
        rimYBottom = rimDrawY + rimDrawH * 2.15f;

        // Проём внутри обода
        rimInnerLeft = rimDrawX + rimDrawW * 0.11f;
        rimInnerRight = rimDrawX + rimDrawW * 0.89f;
        rimW = rimInnerRight - rimInnerLeft;

        // Радиус коллизии стоек обода (точечно, чтобы не залипало)
        rimRadius = Math.max(8f, Math.min(22f, rimDrawH * 0.18f));
        rimLeftX = rimInnerLeft;
        rimRightX = rimInnerRight;

        backboardRect.setEmpty();

        rimFrontPaint.setStrokeWidth(Math.max(8f, rimDrawH * 0.22f));
    }

    private void spawnHoop(int w, int h) {
        hoopX = random.nextInt((int) (w - hoopWidth));
        float minY = h / 12f;
        float maxY = h / 3.5f;
        hoopY = minY + random.nextFloat() * (maxY - minY);
        scored = false;
        enteredHoop = false;
        computeHoopGeometry();
        ensureObstaclesNotBlockingHoop(w, h);
    }

    private void ensureObstaclesNotBlockingHoop(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (obstacleW <= 0 || obstacleH <= 0) return;

        // Защитная зона вокруг кольца + сетки
        float zoneL = hoopX - obstacleW * 0.65f;
        float zoneR = hoopX + hoopWidth + obstacleW * 0.65f;
        float zoneT = hoopY - obstacleH * 2.0f;
        float zoneB = hoopY + hoopHeight + obstacleH * 1.2f;

        if (showObstacle) {
            int guard = 0;
            while (guard++ < 24 && rectsOverlap(obstacleX, obstacleY, obstacleW, obstacleH, zoneL, zoneT, zoneR - zoneL, zoneB - zoneT)) {
                obstacleX = (float) (Math.random() * (w - obstacleW));
                // сохраняем уровень по Y
            }
        }
        if (showObstacle2) {
            int guard = 0;
            while (guard++ < 24 && rectsOverlap(obstacle2X, obstacle2Y, obstacleW, obstacleH, zoneL, zoneT, zoneR - zoneL, zoneB - zoneT)) {
                obstacle2X = (float) (Math.random() * (w - obstacleW));
            }
        }
    }

    private boolean rectsOverlap(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean shaking = System.currentTimeMillis() < shakeUntilMs;
        if (shaking) {
            float t = (shakeUntilMs - System.currentTimeMillis()) / 260f;
            float amp = Math.max(0f, Math.min(1f, t)) * shakeAmpPx;
            float dx = (fxRandom.nextFloat() - 0.5f) * 2f * amp;
            float dy = (fxRandom.nextFloat() - 0.5f) * 2f * amp;
            canvas.save();
            canvas.translate(dx, dy);
        }

        float rimHForNet = rimDrawH;
        float topNetBandY = rimYTop + rimHForNet * 0.88f;
        float bottomNetBandY = hoopY + hoopHeight * 0.82f;
        boolean netOverBall = enteredHoop
                && isMoving
                && (ballY + ballRadius > topNetBandY)
                && (ballY - ballRadius < bottomNetBandY)
                && velocityY > 0;

        if (!netOverBall) {
            drawProceduralHoop(canvas, true);
        }

        // Рисуем мяч
        float ballDrawY = ballY;
        float scale = 1f;
        // Анимация попадания — только у кольца
        if (hitAnim) {
            float t = Math.min(1f, (System.currentTimeMillis() - hitAnimStart) / 160f);
            if (t < 1f) postInvalidate();
            else hitAnim = false;
            float cx = hoopX + hoopWidth / 2f;
            float cy = hoopY + hoopHeight / 2.2f;
            float baseR = hoopWidth * 0.33f;
            flashPaint.setAlpha((int)(180 * (1f - t)));
            canvas.drawCircle(cx, cy, baseR * (1f - t * 0.15f), flashPaint);
            // 10 белых лучей
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI * 2 * i / 10.0;
                float r1 = baseR * 0.7f;
                float r2 = baseR * (2.1f + 0.4f * (1f - t));
                rayPaint.setAlpha((int)(110 * (1f - t)));
                float x1 = cx + (float)Math.cos(angle) * r1;
                float y1 = cy + (float)Math.sin(angle) * r1;
                float x2 = cx + (float)Math.cos(angle) * r2;
                float y2 = cy + (float)Math.sin(angle) * r2;
                canvas.drawLine(x1, y1, x2, y2, rayPaint);
            }
        }
        if (scored && Math.abs(ballX - (hoopX + hoopWidth / 2)) < hoopWidth / 2.5f && ballY > hoopY && ballY < hoopY + hoopHeight) {
            float t = Math.min(1f, (System.currentTimeMillis() - hitAnimStart) / 400f);
            ballDrawY = hoopY + hoopHeight * 0.7f + t * (getHeight() - (hoopY + hoopHeight * 0.7f));
            if (t < 1f) postInvalidate();
            else scored = false;
        }
        float shadowAlpha;
        if (getHeight() > 0) {
            float tShadow = Math.max(0f, Math.min(1f, 1f - (ballY / (float) getHeight())));
            shadowAlpha = 0.12f + 0.28f * tShadow;
        } else {
            shadowAlpha = 0.2f;
        }
        ballShadowPaint.setAlpha((int) (255 * shadowAlpha));
        float shadowY = getHeight() - ballRadius * 0.9f;
        float shadowRx = ballRadius * 0.85f;
        float shadowRy = ballRadius * 0.28f;
        canvas.drawOval(ballX - shadowRx, shadowY - shadowRy, ballX + shadowRx, shadowY + shadowRy, ballShadowPaint);
        // Draw ball (выбранный)
        drawProceduralBall(canvas, ballX, ballDrawY, ballRadius * scale);

        if (netOverBall) {
            drawProceduralHoop(canvas, true);
        }

        // Рисуем корзину (передний слой поверх мяча)
        drawProceduralHoop(canvas, false);
        // Draw score
        float scoreY = getHeight() - 600; // нижний счётчик
        if (onlineMode) {
            Paint lp = new Paint(scorePaint);
            lp.setTextAlign(Paint.Align.LEFT);
            lp.setTextSize(160f); // Крупнее и сочнее
            canvas.drawText(String.valueOf(score), 80f, scoreY, lp);

            Paint rp = new Paint(scorePaint);
            rp.setTextAlign(Paint.Align.RIGHT);
            rp.setTextSize(160f);
            rp.setColor(0xFFB266FF); // Фиолетовый для бота
            rp.setShadowLayer(20, 0, 10, 0xAA2D193C);
            canvas.drawText(String.valueOf(remoteScore), getWidth() - 80f, scoreY, rp);
        } else {
            canvas.drawText(String.valueOf(score), getWidth() / 2f, scoreY, scorePaint);
        }

        // Комбо (ARCADE/TIMED): крупно и над счётом
        if (!onlineMode && comboStreak >= 2 && System.currentTimeMillis() < comboShowUntilMs) {
            Paint combo = new Paint(comboPaint);
            combo.setColor(getComboTierColor(comboStreak));
            combo.setTextAlign(Paint.Align.CENTER);
            combo.setAlpha(235);
            combo.setTextSize(86f);
            combo.setFakeBoldText(true);
            float scoreTextY = getHeight() - 600;
            canvas.drawText("x" + comboStreak, getWidth() / 2f, scoreTextY - 140f, combo);
        }

        if (onboardingText != null) {
            long now = System.currentTimeMillis();
            if (now < onboardingUntilMs) {
                Paint tip = new Paint(timerPaint);
                float t = (onboardingUntilMs - now) / 350f;
                float a = Math.max(0f, Math.min(1f, t));
                tip.setAlpha((int) (220 * a));
                tip.setTextSize(46);
                canvas.drawText(onboardingText, getWidth() / 2f, 130f, tip);
                postInvalidate();
            } else {
                onboardingText = null;
            }
        }

        if (onlineMode) {
            drawBotShot(canvas);
        } else if (ghostState.active) {
            drawGhostBall(canvas);
        }

        drawHud(canvas);
        // Достижения (новый стиль)
        if (achievementText != null && System.currentTimeMillis() - achievementShowTime < 2500) {
            float cx = getWidth() / 2f, cy = getHeight() / 2f;
            float rw = 1100, rh = 700;
            // Карточка popup
            Paint popupBg = new Paint();
            popupBg.setColor(0xFF1A102B);
            popupBg.setStyle(Paint.Style.FILL);
            popupBg.setShadowLayer(48, 0, 16, 0xFF8f5cff);
            canvas.drawRoundRect(cx - rw/2, cy - rh/2, cx + rw/2, cy + rh/2, 80, 80, popupBg);
            Paint popupBorder = new Paint();
            popupBorder.setColor(0xFF8f5cff);
            popupBorder.setStyle(Paint.Style.STROKE);
            popupBorder.setStrokeWidth(24);
            canvas.drawRoundRect(cx - rw/2, cy - rh/2, cx + rw/2, cy + rh/2, 80, 80, popupBorder);
            // Заголовок
            achPaint.setTextSize(88);
            achPaint.setColor(0xFF8f5cff);
            achPaint.setFakeBoldText(true);
            canvas.drawText(achievementText, cx, cy - 210, achPaint);
            // Описание
            achPaint.setTextSize(60);
            achPaint.setColor(0xFFFFFFFF);
            canvas.drawText(achievementDesc != null ? achievementDesc : "", cx, cy + 285, achPaint);
            // Картинка награды — как в инвентаре
            float cardSize = 320;
            float cardY = cy - cardSize/2 + 40;
            float cardX = cx - cardSize/2;
            Paint cardBorder = new Paint();
            cardBorder.setColor(0xFFB266FF);
            cardBorder.setStyle(Paint.Style.STROKE);
            cardBorder.setStrokeWidth(18);
            Paint cardBg = new Paint();
            cardBg.setColor(0xFF23143A);
            cardBg.setStyle(Paint.Style.FILL);
            RectF cardRect = new RectF(cardX, cardY, cardX + cardSize, cardY + cardSize);
            canvas.drawRoundRect(cardRect, 48, 48, cardBg);
            canvas.drawRoundRect(cardRect, 48, 48, cardBorder);
            if (achievementIconRes != 0) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), achievementIconRes);
                RectF iconRect = new RectF(cardX + 32, cardY + 32, cardX + cardSize - 32, cardY + cardSize - 32);
                canvas.drawBitmap(Bitmap.createScaledBitmap(icon, (int)(cardSize-64), (int)(cardSize-64), false), null, iconRect, null);
            } else if (achievementDesc != null && achievementDesc.contains("синий")) {
                Paint bgPaint = new Paint();
                bgPaint.setShader(new android.graphics.LinearGradient(cardX, cardY, cardX + cardSize, cardY + cardSize, 0xFF3A2B5C, 0xFF2D193C, android.graphics.Shader.TileMode.CLAMP));
                canvas.drawRoundRect(cardRect, 48, 48, bgPaint);
                canvas.drawRoundRect(cardRect, 48, 48, cardBorder);
            } else if (achievementDesc != null && achievementDesc.contains("оранжевый")) {
                Paint bgPaint = new Paint();
                bgPaint.setShader(new android.graphics.LinearGradient(cardX, cardY, cardX + cardSize, cardY + cardSize, 0xFFFFA726, 0xFFFF7043, android.graphics.Shader.TileMode.CLAMP));
                canvas.drawRoundRect(cardRect, 48, 48, bgPaint);
                canvas.drawRoundRect(cardRect, 48, 48, cardBorder);
            }
            if (System.currentTimeMillis() - achievementShowTime < 2500) postInvalidate();
        }
        if (showObstacle) {
            canvas.drawRoundRect(obstacleX, obstacleY, obstacleX + obstacleW, obstacleY + obstacleH, 24, 24, obsPaint);
        }
        if (showObstacle2) {
            canvas.drawRoundRect(obstacle2X, obstacle2Y, obstacle2X + obstacleW, obstacle2Y + obstacleH, 24, 24, obsPaint2);
        }
        // Кнопка "Заново"
        if (showResetButton) {
            float cx = getWidth() / 2f;
            float cy = getHeight() - ballRadius * 2;
            float r = ballRadius * 1.1f;
            canvas.drawCircle(cx, cy, r, btnPaint);
            // Иконка обновления (круговая стрелка)
            canvas.drawArc(cx - r * 0.5f, cy - r * 0.5f, cx + r * 0.5f, cy + r * 0.5f, 60, 270, false, iconPaint);
            float angle = (float) Math.toRadians(60 + 270);
            float x1 = (float) (cx + Math.cos(angle) * r * 0.5f);
            float y1 = (float) (cy + Math.sin(angle) * r * 0.5f);
            float arrowLen = r * 0.18f;
            float arrowAngle1 = (float) (angle - Math.PI / 7);
            float arrowAngle2 = (float) (angle + Math.PI / 7);
            float ax1 = (float) (x1 - Math.cos(arrowAngle1) * arrowLen);
            float ay1 = (float) (y1 - Math.sin(arrowAngle1) * arrowLen);
            float ax2 = (float) (x1 - Math.cos(arrowAngle2) * arrowLen);
            float ay2 = (float) (y1 - Math.sin(arrowAngle2) * arrowLen);
            canvas.drawLine(x1, y1, ax1, ay1, iconPaint);
            canvas.drawLine(x1, y1, ax2, ay2, iconPaint);
        }

        // Impact flash (после всех объектов)
        long now = System.currentTimeMillis();
        if (now < impactFlashUntilMs) {
            float t = (impactFlashUntilMs - now) / 140f;
            float a = Math.max(0f, Math.min(1f, t));
            impactFlashPaint.setColor(0xFFFFFFFF);
            impactFlashPaint.setAlpha((int) (110 * a));
            canvas.drawRect(0, 0, getWidth(), getHeight(), impactFlashPaint);
            postInvalidate();
        }

        if (shaking) {
            canvas.restore();
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (showResetButton) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float cx = getWidth() / 2f;
                float cy = getHeight() - ballRadius * 2;
                float r = ballRadius * 1.1f;
                float dx = event.getX() - cx;
                float dy = event.getY() - cy;
                if (dx*dx + dy*dy <= r*r) {
                    // Анимация нажатия
                    this.animate().scaleX(0.8f).scaleY(0.8f).setDuration(60).withEndAction(() -> {
                        this.animate().scaleX(1f).scaleY(1f).setDuration(60).start();
                        showResetButton = false;
                        resetAfterMiss();
                        invalidate();
                    }).start();
                    return true;
                }
            }
        }
        if (gameOver) return false;
        if (!canSwipe) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                endX = event.getX();
                endY = event.getY();
                float dx = endX - startX;
                float dy = endY - startY;

                // Дуэль-бот стартует только после первого броска игрока
                maybeStartOpponentAfterFirstShot();

                velocityX = dx / 2.2f;
                velocityY = dy / 2.2f;

                float maxAxis = Math.max(420f, getWidth() / 9f);
                velocityX = clamp(velocityX, -maxAxis, maxAxis);
                velocityY = clamp(velocityY, -maxAxis, maxAxis);

                float maxTotal = Math.max(560f, getWidth() / 6f);
                float v = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                if (v > maxTotal && v > 0.0001f) {
                    float s = maxTotal / v;
                    velocityX *= s;
                    velocityY *= s;
                }
                isMoving = true;
                canSwipe = false;
                showResetButton = true;
                postInvalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (paused) return;
        boolean needInvalidate = false;
        if (movingHoop) {
            hoopX += hoopVX;
            if (hoopX < 0 || hoopX + hoopWidth > getWidth()) hoopVX = -hoopVX;
            computeHoopGeometry();
            needInvalidate = true;
        }
        if (showObstacle) {
            obstacleX += obstacleVX;
            if (obstacleX < 0 || obstacleX + obstacleW > getWidth()) obstacleVX = -obstacleVX;
            needInvalidate = true;
        }
        if (showObstacle2) {
            obstacle2X += obstacle2VX;
            if (obstacle2X < 0 || obstacle2X + obstacleW > getWidth()) obstacle2VX = -obstacle2VX;
            needInvalidate = true;
        }
        if (isMoving) {
            float prevBallY = ballY;
            float prevBallX = ballX;

            int steps = 1;
            float speed = Math.abs(velocityX) + Math.abs(velocityY);
            if (speed > getWidth() / 16f) steps = 3;
            else if (speed > getWidth() / 22f) steps = 2;

            float stepScale = 1f / (float) steps;
            float stepGravity = GRAVITY * stepScale;
            float stepFriction = (float) Math.pow(FRICTION, stepScale);

            for (int i = 0; i < steps; i++) {
                float stepPrevY = ballY;
                float stepPrevX = ballX;
            // Столкновение с препятствием 1 (сверху, снизу, сбоку)
            if (showObstacle) {
                float obsLeft = obstacleX + obstacleW * 0.15f;
                float obsRight = obstacleX + obstacleW * 0.85f;
                boolean hitTop = ballY + ballRadius > obstacleY && ballY - ballRadius < obstacleY + obstacleH && ballX + ballRadius > obsLeft && ballX - ballRadius < obsRight && velocityY > 0;
                boolean hitBottom = ballY - ballRadius < obstacleY + obstacleH && ballY + ballRadius > obstacleY && ballX + ballRadius > obsLeft && ballX - ballRadius < obsRight && velocityY < 0;
                boolean hitLeft = ballX + ballRadius > obsLeft && ballX < obsLeft && ballY + ballRadius > obstacleY && ballY - ballRadius < obstacleY + obstacleH && velocityX > 0;
                boolean hitRight = ballX - ballRadius < obsRight && ballX > obsRight && ballY + ballRadius > obstacleY && ballY - ballRadius < obstacleY + obstacleH && velocityX < 0;
                if (hitTop) {
                    velocityY = -Math.abs(velocityY) * 1.2f;
                    ballY = obstacleY - ballRadius;
                } else if (hitBottom) {
                    velocityY = Math.abs(velocityY) * 1.2f;
                    ballY = obstacleY + obstacleH + ballRadius;
                } else if (hitLeft) {
                    velocityX = -Math.abs(velocityX) * 1.2f;
                    ballX = obsLeft - ballRadius;
                } else if (hitRight) {
                    velocityX = Math.abs(velocityX) * 1.2f;
                    ballX = obsRight + ballRadius;
                }
            }
            // Столкновение с препятствием 2 (сверху, снизу, сбоку)
            if (showObstacle2) {
                float obs2Left = obstacle2X + obstacleW * 0.15f;
                float obs2Right = obstacle2X + obstacleW * 0.85f;
                boolean hitTop2 = ballY + ballRadius > obstacle2Y && ballY - ballRadius < obstacle2Y + obstacleH && ballX + ballRadius > obs2Left && ballX - ballRadius < obs2Right && velocityY > 0;
                boolean hitBottom2 = ballY - ballRadius < obstacle2Y + obstacleH && ballY + ballRadius > obstacle2Y && ballX + ballRadius > obs2Left && ballX - ballRadius < obs2Right && velocityY < 0;
                boolean hitLeft2 = ballX + ballRadius > obs2Left && ballX < obs2Left && ballY + ballRadius > obstacle2Y && ballY - ballRadius < obstacle2Y + obstacleH && velocityX > 0;
                boolean hitRight2 = ballX - ballRadius < obs2Right && ballX > obs2Right && ballY + ballRadius > obstacle2Y && ballY - ballRadius < obstacle2Y + obstacleH && velocityX < 0;
                if (hitTop2) {
                    velocityY = -Math.abs(velocityY) * 1.2f;
                    ballY = obstacle2Y - ballRadius;
                } else if (hitBottom2) {
                    velocityY = Math.abs(velocityY) * 1.2f;
                    ballY = obstacle2Y + obstacleH + ballRadius;
                } else if (hitLeft2) {
                    velocityX = -Math.abs(velocityX) * 1.2f;
                    ballX = obs2Left - ballRadius;
                } else if (hitRight2) {
                    velocityX = Math.abs(velocityX) * 1.2f;
                    ballX = obs2Right + ballRadius;
                }
            }
            ballX += velocityX * stepScale;
            ballY += velocityY * stepScale;
            velocityY += stepGravity;
            velocityX *= stepFriction;
            velocityY *= stepFriction;

            float maxTotal = Math.max(560f, getWidth() / 6f);
            float v = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (v > maxTotal && v > 0.0001f) {
                float s = maxTotal / v;
                velocityX *= s;
                velocityY *= s;
            }
            // Отскок от боковых границ
            if (ballX - ballRadius < 0) {
                ballX = ballRadius;
                velocityX = -velocityX * 0.9f;
            } else if (ballX + ballRadius > getWidth()) {
                ballX = getWidth() - ballRadius;
                velocityX = -velocityX * 0.9f;
            }

                handleRimCollision();
                handleBackboardCollision();
                handleFairScoring(stepPrevX, stepPrevY);

                // Любой контакт/пролёт в зоне сетки даёт импульс, чтобы она "жила" как в реальности.
                maybeApplyNetBallContactImpulse();

                // Сетка реагирует на пролёт мяча рядом с кольцом (даже без касания обода)
                float cx = hoopX + hoopWidth / 2f;
                float cy = rimYTop + rimDrawH * 0.7f;
                float dx = ballX - cx;
                float dy = ballY - cy;
                float dist2 = dx * dx + dy * dy;
                float thr = hoopWidth * 0.62f;
                if (dist2 < thr * thr) {
                    float dist = (float) Math.sqrt(Math.max(0.0001f, dist2));
                    float near = 1f - Math.min(1f, dist / thr);
                    float dir = dx >= 0 ? 1f : -1f;
                    float impulse = (3.5f + near * 7.5f) * dir;
                    for (int n = 0; n < NET_NODES; n++) {
                        float tt = n / (float) (NET_NODES - 1);
                        float w = 0.22f + 0.78f * tt;
                        netVx[n] += impulse * w * near;
                    }
                }

                prevBallY = stepPrevY;
                prevBallX = stepPrevX;
            }
            // Если мяч не попал, но опустился ниже кольца — падает вниз
            if (ballY > getHeight() - ballRadius) {
                resetAfterMiss();
                showResetButton = false;
            }
            // Если мяч улетел вверх — возвращаем вниз, но кнопку не скрываем
            if (ballY < 0) {
                velocityY = Math.abs(velocityY) * 0.85f;
                ballY = ballRadius;
                // showResetButton = false; // не скрываем кнопку
            }
            // Анимация достижения исчезает через 2 сек
            if (showAchievement && System.currentTimeMillis() - achievementTime > 2000) {
                showAchievement = false;
            }
            // После подсчёта очков — включаем усложнения
            applyArcadeDifficulty();
            needInvalidate = true;
        }
        if (timedMode || onlineMode) {
            updateTimer();
            needInvalidate = true;
            if (onlineMode) {
                maybeSendSnapshot();
            }
        }
        if (needInvalidate) postInvalidate();
    }

    private void maybeApplyNetBallContactImpulse() {
        if (gameOver) return;
        long now = System.currentTimeMillis();
        if (now - lastNetBallImpulseAtMs < 70L) return;

        // Геометрия области сетки должна совпадать с drawHoopLikePng(back layer)
        float topNetY = rimDrawY + rimDrawH * 0.85f;
        float bottomNetY = hoopY + hoopHeight * 0.80f;
        if (ballY + ballRadius < topNetY || ballY - ballRadius > bottomNetY) return;

        float rimX = rimDrawX;
        float rimW = rimDrawW;
        float topL = rimX + rimW * 0.10f;
        float topR = rimX + rimW * 0.90f;
        float botL = hoopX + hoopWidth * 0.28f;
        float botR = hoopX + hoopWidth * 0.72f;

        float t = (ballY - topNetY) / Math.max(1f, (bottomNetY - topNetY));
        t = clamp(t, 0f, 1f);
        float left = lerp(topL, botL, t);
        float right = lerp(topR, botR, t);
        float pad = ballRadius * 0.35f;

        if (ballX + ballRadius < left - pad || ballX - ballRadius > right + pad) return;

        // Импульс зависит от скорости и смещения от центра
        float center = (left + right) * 0.5f;
        float dx = clamp((ballX - center) / Math.max(1f, (right - left)), -1f, 1f);
        float speed = Math.abs(velocityX) + Math.abs(velocityY);
        float strength = clamp(speed / Math.max(1f, getWidth() / 6f), 0.15f, 1.1f);

        float dir = dx >= 0 ? 1f : -1f;
        float impulseX = (8.0f + 18.0f * strength) * dir;

        int centerNode = Math.round(t * (NET_NODES - 1));
        centerNode = Math.max(0, Math.min(NET_NODES - 1, centerNode));

        float down = (10.0f + 26.0f * strength);
        if (velocityY < 0) down *= 0.55f;
        for (int i = 0; i < NET_NODES; i++) {
            float d = Math.abs(i - centerNode);
            float falloff = 1f / (1f + d * d * 1.15f);
            float edge = i / (float) (NET_NODES - 1);
            float w = 0.35f + 0.65f * edge;

            netVx[i] += impulseX * w * falloff * (0.65f + 0.35f * Math.abs(dx));
            netVy[i] += down * (0.55f + 0.45f * edge) * falloff;
        }

        lastNetBallImpulseAtMs = now;
    }

    private void resetBall() {
        if (gameOver) return;
        ballX = getWidth() / 2f;
        ballY = getHeight() - ballRadius * 2;
        isMoving = false;
        velocityX = 0;
        velocityY = 0;
        scored = false;
        enteredHoop = false;
        canSwipe = true;
    }

    private void resetAfterMiss() {
        comboStreak = 0;
        comboShowUntilMs = 0L;
        resetBall();
    }

    private void handleRimCollision() {
        if (gameOver) return;
        // Если мяч летит вверх и находится ниже обода, отключаем коллизию, 
        // чтобы он не бился "об низ" корзины при броске
        if (velocityY < 0 && ballY > rimYTop - ballRadius) return;
        
        float rimY = rimYTop;

        float cxL = rimLeftX;
        float cyL = rimY;
        float cxR = rimRightX;
        float cyR = rimY;

        boolean hitL = resolveCircleCollision(cxL, cyL, rimRadius);
        boolean hitR = resolveCircleCollision(cxR, cyR, rimRadius);

        if ((hitL || hitR) && !gameOver) {
            long now = System.currentTimeMillis();
            if (now >= rimShakeUntilMs) rimShakeStartMs = now;
            rimShakeUntilMs = Math.max(rimShakeUntilMs, now + 260L);
            float base = Math.max(3.0f, rimRadius * 0.36f);
            rimShakeAmpPx = Math.max(rimShakeAmpPx, Math.min(22f, base));
        }

        // Импульс сетке даже от касания обода — чтобы она "жила" визуально
        if ((hitL || hitR) && !scored) {
            float dir = (hitR ? 1f : -1f);
            float impulse = (10f + fxRandom.nextFloat() * 8f) * dir;
            for (int i = 0; i < NET_NODES; i++) {
                float t = i / (float) (NET_NODES - 1);
                float w = 0.18f + 0.82f * t;
                netVx[i] += impulse * w;
                netVy[i] += (6.0f + fxRandom.nextFloat() * 4.0f) * (0.35f + 0.65f * t);
            }
            postInvalidate();
        }

        // Важное правило: если мяч уже зашёл в кольцо и падает вниз,
        // НЕ сбрасываем enteredHoop от касаний обода — иначе гол не засчитывается.
        if (!enteredHoop) {
            if (hitL || hitR) {
                // До входа в кольцо касание обода сбрасывает попытку.
                enteredHoop = false;
            }
        }
    }

    private boolean resolveCircleCollision(float cx, float cy, float r) {
        float dx = ballX - cx;
        float dy = ballY - cy;
        float dist2 = dx * dx + dy * dy;
        float minDist = ballRadius + r;
        float minDist2 = minDist * minDist;
        if (dist2 >= minDist2) return false;

        float dist = (float) Math.sqrt(Math.max(0.0001f, dist2));
        float nx = dx / dist;
        float ny = dy / dist;

        float penetration = minDist - dist;
        ballX += nx * penetration;
        ballY += ny * penetration;

        float vn = velocityX * nx + velocityY * ny;
        if (vn < 0) {
            float bounce = 0.82f;
            velocityX = velocityX - (1f + bounce) * vn * nx;
            velocityY = velocityY - (1f + bounce) * vn * ny;
        }
        return true;
    }

    private void handleBackboardCollision() {
        // no-op: визуально щита нет, коллизии отключены
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private void handleFairScoring(float prevX, float prevY) {
        if (gameOver) return;
        if (scored) return;
        if (velocityY <= 0) return;

        // Чуть шире, чтобы визуальный проход соответствовал геометрии обода
        float left = rimInnerLeft + ballRadius * 0.25f;
        float right = rimInnerRight - ballRadius * 0.25f;
        if (left >= right) return;

        float prevBottom = prevY + ballRadius;
        float currBottom = ballY + ballRadius;
        boolean crossesTopDown = prevBottom < rimYTop && currBottom >= rimYTop;
        if (crossesTopDown && ballX > left && ballX < right) {
            enteredHoop = true;
        }

        if (enteredHoop) {
            float prevTop = prevY - ballRadius;
            float currTop = ballY - ballRadius;
            boolean passesThrough = prevTop < rimYBottom && currTop >= rimYBottom;
            if (passesThrough && ballX > left && ballX < right) {
                onScored();
            }
        }
    }

    private void onScored() {
        int add = 1;
        // В TIMED поощряем скилл: комбо даёт бонусное очко.
        // ONLINE_DUEL не трогаем, чтобы матч оставался честным и предсказуемым.
        if (gameMode == GameMode.TIMED && comboStreak >= 3) {
            add = 2;
        }
        score += add;
        comboStreak++;
        if (comboStreak > bestComboStreak) bestComboStreak = comboStreak;
        comboShowUntilMs = (comboStreak >= 2) ? (System.currentTimeMillis() + 1400) : 0L;

        // Мини-эффект на каждый гол (даже без комбо)
        impactFlashUntilMs = Math.max(impactFlashUntilMs, System.currentTimeMillis() + 70);

        if (comboStreak >= 5) {
            shakeUntilMs = System.currentTimeMillis() + 260;
            shakeAmpPx = Math.min(26f, 8f + (comboStreak - 5) * 3f);
            impactFlashUntilMs = System.currentTimeMillis() + 140;
        } else if (comboStreak >= 3) {
            impactFlashUntilMs = System.currentTimeMillis() + 90;
        }

        vibrateOnScore();
        scored = true;

        float dir = velocityX >= 0 ? 1f : -1f;
        float impulse = 14f * dir + (fxRandom.nextFloat() - 0.5f) * 6f;
        for (int i = 0; i < NET_NODES; i++) {
            float t = i / (float) (NET_NODES - 1);
            float w = 0.25f + 0.75f * t;
            netVx[i] += impulse * w;
        }

        enteredHoop = false;
        showResetButton = false;

        hitAnim = true;
        hitAnimStart = System.currentTimeMillis();

        resetBall();
        spawnHoop(getWidth(), getHeight());

        applyArcadeDifficulty();

        if (score > 0 && score % 3 == 0 && achievementLevel < 8) {
            achievementLevel++;
            SharedPreferences prefs = getContext().getSharedPreferences("basketball", Context.MODE_PRIVATE);
            prefs.edit().putInt("achievementLevel", achievementLevel).apply();

            int cycle = (achievementLevel - 1) % 3;
            if (cycle == 0) {
                if (achievementLevel == 1) {
                    unlockedBall = Math.max(unlockedBall, 1);
                    prefs.edit().putInt("unlockedBall", unlockedBall).apply();
                    achievementText = "Стритболер";
                    achievementDesc = "Открыт мяч: Стрит";
                    achievementIconRes = R.drawable.ball2;
                } else if (achievementLevel == 4) {
                    unlockedBall = Math.max(unlockedBall, 2);
                    prefs.edit().putInt("unlockedBall", unlockedBall).apply();
                    achievementText = "Легенда площадки";
                    achievementDesc = "Открыт мяч: Легенда";
                    achievementIconRes = R.drawable.ball3;
                } else if (achievementLevel == 7) {
                    unlockedBg = Math.max(unlockedBg, 1);
                    prefs.edit().putInt("unlockedBg", unlockedBg).apply();
                    achievementText = "Синий стиль";
                    achievementDesc = "Открыт синий фон!";
                    achievementIconRes = 0;
                }
            } else if (cycle == 1) {
                if (achievementLevel == 2) {
                    unlockedHoop = Math.max(unlockedHoop, 1);
                    prefs.edit().putInt("unlockedHoop", unlockedHoop).apply();
                    achievementText = "Сеточный мастер";
                    achievementDesc = "Открыта сетка: Стрит";
                    achievementIconRes = R.drawable.hoop2;
                } else if (achievementLevel == 5) {
                    unlockedHoop = Math.max(unlockedHoop, 2);
                    prefs.edit().putInt("unlockedHoop", unlockedHoop).apply();
                    achievementText = "Сеточный чемпион";
                    achievementDesc = "Открыта сетка: Легенда";
                    achievementIconRes = R.drawable.hoop3;
                } else if (achievementLevel == 8) {
                    unlockedBg = 2;
                    prefs.edit().putInt("unlockedBg", unlockedBg).apply();
                    achievementText = "Оранжевый стиль";
                    achievementDesc = "Открыт оранжевый фон!";
                    achievementIconRes = 0;
                }
            } else if (cycle == 2) {
                if (achievementLevel == 3) {
                    achievementText = "Коллекционер мячей";
                    achievementDesc = "Все мячи теперь ваши!";
                    achievementIconRes = R.drawable.ball3;
                } else if (achievementLevel == 6) {
                    achievementText = "Властелин сеток";
                    achievementDesc = "Все сетки теперь ваши!";
                    achievementIconRes = R.drawable.hoop3;
                }
            }

            achievementShowTime = System.currentTimeMillis();
            invalidate();
        }
    }

    private void updateTimer() {
        if (gameOver || paused) return;
        long elapsed = System.currentTimeMillis() - modeStartMs;
        remainingTimeMs = Math.max(0, modeDurationMs - elapsed);
        if (remainingTimeMs == 0) {
            finishSession();
        }
    }

    private void finishSession() {
        if (gameOver) return;
        gameOver = true;
        paused = true;
        showResetButton = false;
        canSwipe = false;
        isMoving = false;
        if (sessionListener != null) {
            sessionListener.onSessionComplete(gameMode, score, remoteScore);
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setPaused(boolean value) {
        paused = value;
        if (!paused) postInvalidate();
    }

    public void setDeferOpponentStart(boolean defer) {
        deferOpponentStart = defer;
        // Если включаем defer после того, как matchClient уже установлен,
        // нужно сбросить флаг старта, иначе connect() никогда не вызовется.
        opponentStarted = !deferOpponentStart;
    }

    private void maybeStartOpponentAfterFirstShot() {
        if (!onlineMode) return;
        if (!deferOpponentStart) return;
        if (opponentStarted) return;
        if (matchClient == null) return;
        try {
            matchClient.connect(opponentConnectTarget != null ? opponentConnectTarget : "demo");
            opponentStarted = true;
        } catch (Exception ignored) {
        }
    }

    private void drawHud(Canvas canvas) {
        if (!(timedMode || onlineMode)) return;

        float pad = 24f;
        float top = 260f; // Спустили ниже (было 210)
        float pillH = 120f;
        float pillW = Math.min(getWidth() * 0.40f, 520f);
        float r = 34f;

        Paint text = new Paint(timerPaint);
        text.setTextSize(54f);
        text.setFakeBoldText(true);

        if (onlineMode) {
            // Дуэль: сверху показываем только таймер.
            long now = System.currentTimeMillis();
            int sec = (int) Math.ceil(Math.max(0, remainingTimeMs) / 1000f);
            Paint timeBig = new Paint(timerPaint);
            timeBig.setTextAlign(Paint.Align.CENTER);
            timeBig.setTextSize(120f); // Крупные красивые цифры
            timeBig.setFakeBoldText(true);
            canvas.drawText(String.valueOf(sec), getWidth() / 2f, top + pillH * 0.85f, timeBig);

            // Комбо по сторонам (выше над счетчиками)
            float comboY = getHeight() - 780f; // Подняли (было -690)
            Paint combo = new Paint(timerPaint);
            combo.setTextSize(64f); // Сделали больше (было 44)
            combo.setFakeBoldText(true);

            if (comboStreak >= 2 && now < comboShowUntilMs) {
                combo.setTextAlign(Paint.Align.LEFT);
                combo.setColor(getComboTierColor(comboStreak));
                canvas.drawText("x" + comboStreak, 80f, comboY, combo);
            }
            if (remoteComboStreak >= 2 && now < remoteComboShowUntilMs) {
                combo.setTextAlign(Paint.Align.RIGHT);
                combo.setColor(0xFFB266FF);
                canvas.drawText("x" + remoteComboStreak, getWidth() - 80f, comboY, combo);
            }
        } else {
            // Timed: score уже рисуется внизу.
            // Сверху рисуем только время — крупно по центру.
            long now = System.currentTimeMillis();
            int sec = (int) Math.ceil(Math.max(0, remainingTimeMs) / 1000f);
            Paint timeBig = new Paint(timerPaint);
            timeBig.setTextAlign(Paint.Align.CENTER);
            timeBig.setTextSize(140f);
            timeBig.setFakeBoldText(true);
            canvas.drawText(String.valueOf(sec), getWidth() / 2f, top + pillH * 0.85f, timeBig);

            if (comboStreak >= 2 && now < comboShowUntilMs) {
                Paint combo = new Paint(timerPaint);
                combo.setTextSize(72f); // Сделали больше (было 54)
                combo.setFakeBoldText(true);
                combo.setColor(getComboTierColor(comboStreak));
                combo.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("x" + comboStreak, getWidth() / 2f, top + pillH + 100f, combo); // Подняли
            }
        }
    }

    private void drawGhostBall(Canvas canvas) {
        int a = ghostState.moving ? 150 : 110;
        canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), a);
        float r = ballRadius * 0.92f;
        renderBallLikePng(canvas, selectedBall, ghostState.x, ghostState.y, r);
        canvas.restore();
    }

    private String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public void applyGhostSnapshot(float x, float y, boolean moving) {
        if (!onlineMode) return;
        if (paused || gameOver) return;
        ghostState.x = x;
        ghostState.y = y;
        ghostState.moving = moving;
        ghostState.active = true;
        ghostState.lastUpdate = System.currentTimeMillis();

        // Видимый «промах» бота: если бот двигается, но счёт не меняется, периодически показываем бросок мимо.
        long now = System.currentTimeMillis();
        if (moving && !botShotActive && now - lastBotMissAnimAtMs > 1400L) {
            // небольшой шанс, чтобы не спамило
            if (fxRandom.nextFloat() < 0.35f) {
                startBotMissShotAnimation();
                lastBotMissAnimAtMs = now;

                // Промах должен сбрасывать серию бота
                remoteComboStreak = 0;
                remoteComboShowUntilMs = 0L;
            }
        }
        postInvalidate();
    }

    private long lastBotMissAnimAtMs = 0L;

    private void startBotMissShotAnimation() {
        long now = System.currentTimeMillis();
        botShotActive = true;
        botShotStartMs = now;

        botShotDurMs = 460L + fxRandom.nextInt(280);

        botFromX = getWidth() * (0.62f + fxRandom.nextFloat() * 0.22f);
        botFromY = getHeight() - ballRadius * (2.0f + fxRandom.nextFloat() * 0.45f);

        // Мимо кольца: сдвигаем цель чуть влево/вправо и ниже
        float missSide = fxRandom.nextBoolean() ? 1f : -1f;
        botToX = hoopX + hoopWidth / 2f + missSide * hoopWidth * (0.42f + fxRandom.nextFloat() * 0.18f);
        botToY = rimYTop + rimDrawH * (1.55f + fxRandom.nextFloat() * 0.25f);

        botPeakY = Math.min(botFromY, botToY) - Math.max(getHeight() * 0.18f, 440f) - fxRandom.nextFloat() * 220f;

        botSwipeX1 = botFromX;
        botSwipeY1 = botFromY;
        botSwipeX2 = botFromX - (botFromX - botToX) * 0.35f;
        botSwipeY2 = botFromY - Math.max(360f, getHeight() * 0.20f);
        botSwipeUntilMs = now + 220L;
    }

    public void updateRemoteScore(int value) {
        if (!onlineMode) return;
        if (paused || gameOver) return;
        if (value > remoteScore) {
            int delta = value - remoteScore;
            remoteComboStreak++;
            remoteComboShowUntilMs = System.currentTimeMillis() + 1400L;
            planBotShotAnimation(delta);
            botPendingHoopMoveOnScore = true;
        } else if (value < remoteScore) {
            remoteComboStreak = 0;
            remoteComboShowUntilMs = 0L;
        }
        remoteScore = value;
        lastRemoteScore = value;
        postInvalidate();
    }

    private void planBotShotAnimation(int delta) {
        long now = System.currentTimeMillis();
        botShotPlanned = true;
        botShotPlannedDelta = delta;
        botShotPlanAtMs = now + (140L + fxRandom.nextInt(420));
    }

    private void startBotShotAnimationNow(int delta) {
        long now = System.currentTimeMillis();
        botShotActive = true;
        botShotStartMs = now;

        // Вариативность длительности
        botShotDurMs = 440L + fxRandom.nextInt(260);

        // Старт бота (право/центр, чуть гуляем)
        botFromX = getWidth() * (0.64f + fxRandom.nextFloat() * 0.18f);
        botFromY = getHeight() - ballRadius * (2.0f + fxRandom.nextFloat() * 0.35f);

        // Цель — около центра кольца, но с заметной «человеческой» погрешностью
        // (визуально не должно выглядеть как идеальный лазер в одну точку)
        float jitterX = (fxRandom.nextFloat() - 0.5f) * hoopWidth * 0.32f;
        float jitterY = (fxRandom.nextFloat() - 0.5f) * rimDrawH * 0.55f;
        botToX = hoopX + hoopWidth / 2f + jitterX;
        botToY = rimYTop + rimDrawH * 0.55f + jitterY;

        float peakBase = Math.max(getHeight() * 0.20f, 460f);
        botPeakY = Math.min(botFromY, botToY) - (peakBase + fxRandom.nextFloat() * 260f);

        // Линия "свайпа"
        botSwipeX1 = botFromX;
        botSwipeY1 = botFromY;
        botSwipeX2 = botFromX - (botFromX - botToX) * 0.35f;
        botSwipeY2 = botFromY - Math.max(320f, getHeight() * (0.18f + fxRandom.nextFloat() * 0.08f));
        botSwipeUntilMs = now + 220L;
    }

    private void drawBotShot(Canvas canvas) {
        if (!botShotActive) {
            if (botShotPlanned && !paused && !gameOver) {
                long now = System.currentTimeMillis();
                if (now >= botShotPlanAtMs) {
                    botShotPlanned = false;
                    startBotShotAnimationNow(botShotPlannedDelta);
                }
            }
            return;
        }
        if (paused || gameOver) { botShotActive = false; return; }
        long now = System.currentTimeMillis();
        float t = (now - botShotStartMs) / (float) botShotDurMs;
        if (t >= 1f) {
            botShotActive = false;

            // После гола бота — переносим кольцо как после гола игрока.
            if (botPendingHoopMoveOnScore) {
                botPendingHoopMoveOnScore = false;

                // Лёгкий импульс сетке для ощущения «прошло через кольцо»
                float dir = fxRandom.nextBoolean() ? 1f : -1f;
                float impulse = 11f * dir + (fxRandom.nextFloat() - 0.5f) * 6f;
                for (int i = 0; i < NET_NODES; i++) {
                    float tt = i / (float) (NET_NODES - 1);
                    float w = 0.22f + 0.78f * tt;
                    netVx[i] += impulse * w;
                }

                spawnHoop(getWidth(), getHeight());
                computeHoopGeometry();
            }
            return;
        }

        // Парабола: (1-t)^2*from + 2(1-t)t*peak + t^2*to
        float inv = 1f - t;
        float midX = (botFromX + botToX) * 0.5f + (fxRandom.nextFloat() - 0.5f) * hoopWidth * 0.10f;
        botBallX = inv * inv * botFromX + 2f * inv * t * midX + t * t * botToX;
        botBallY = inv * inv * botFromY + 2f * inv * t * botPeakY + t * t * botToY;

        // Рисуем свайп-трейл
        if (now < botSwipeUntilMs) {
            Paint p = new Paint(timerPaint);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(10f);
            p.setColor(0xFFB266FF);
            p.setAlpha(160);
            canvas.drawLine(botSwipeX1, botSwipeY1, botSwipeX2, botSwipeY2, p);
        }

        // Рисуем мяч бота (фиолетовый контур)
        canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), 170);
        int prev = selectedBall;
        // временно рисуем как "легенда" (фиолетовый)
        renderBallLikePng(canvas, 2, botBallX, botBallY, ballRadius * 0.92f);
        canvas.restore();

        // Поддерживаем ghostState для совместимости
        ghostState.x = botBallX;
        ghostState.y = botBallY;
        ghostState.moving = true;
        ghostState.active = true;
    }

    public void setMatchClient(MatchClient client) {
        this.matchClient = client;
        // По умолчанию — сразу стартуем. Для бота GameActivity выставляет defer=true и connect вызывается после первого броска.
        opponentStarted = (client != null) && !deferOpponentStart;
    }

    public void setOpponentConnectTarget(String target) {
        opponentConnectTarget = target;
    }

    public void setSessionListener(SessionListener listener) {
        this.sessionListener = listener;
    }

    public int getScore() {
        return score;
    }

    public int getRemoteScore() {
        return remoteScore;
    }

    private void maybeSendSnapshot() {
        if (matchClient == null || !onlineMode) return;
        long now = System.currentTimeMillis();
        if (now - lastSnapshotSent < 60) return;
        matchClient.sendSnapshot(ballX, ballY, isMoving, score);
        lastSnapshotSent = now;
    }

    private void vibrateOnScore() {
        SharedPreferences prefs = getContext().getSharedPreferences("basketball", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("vibrationEnabled", true);
        if (!enabled) return;
        try {
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) getContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                vibrator = vm != null ? vm.getDefaultVibrator() : null;
            } else {
                vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            }
            if (vibrator == null || !vibrator.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(25);
            }
        } catch (Exception ignored) {
        }
    }

    private static class GhostState {
        float x;
        float y;
        boolean moving;
        boolean active;
        long lastUpdate;

        void reset() {
            active = false;
            moving = false;
            x = 0;
            y = 0;
            lastUpdate = 0;
        }
    }
    public void setSelectedBall(int idx) {
        selectedBall = idx;
        // Сохраняем выбор
        SharedPreferences prefs = getContext().getSharedPreferences("basketball", Context.MODE_PRIVATE);
        prefs.edit().putInt("selectedBall", idx).apply();
    }

    public static void animateButton(View button) {
        button.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).withEndAction(() ->
            button.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
        ).start();
    }
} 