package com.example.basketballgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.Random;

/**
 * Накладывается поверх контента и рисует падающие конфетти.
 * Использование:
 * <pre>
 *   ConfettiView v = new ConfettiView(ctx);
 *   rootLayout.addView(v, MATCH_PARENT / MATCH_PARENT);
 *   v.start(4000); // анимация 4 секунды, затем view удаляет себя сама
 * </pre>
 */
public class ConfettiView extends View {

    private static final int COUNT = 90;

    private static final int[] PALETTE = {
        0xFFE91E63, 0xFFFFEB3B, 0xFF4CAF50, 0xFF2196F3,
        0xFFFF9800, 0xFFB266FF, 0xFFFF5722, 0xFF00BCD4,
        0xFFFFFFFF, 0xFF8BC34A
    };

    private final float[] x      = new float[COUNT];
    private final float[] y      = new float[COUNT];
    private final float[] vx     = new float[COUNT];
    private final float[] vy     = new float[COUNT];
    private final float[] rot    = new float[COUNT];
    private final float[] rotSpd = new float[COUNT];
    private final float[] pw     = new float[COUNT];
    private final float[] ph     = new float[COUNT];
    private final int[]   color  = new int[COUNT];

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Runnable tickRunnable = this::tick;
    private boolean running;
    private long stopAtMs;

    public ConfettiView(Context ctx) {
        super(ctx);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /** Запустить конфетти на {@code durationMs} миллисекунд, затем удалить себя из родителя. */
    public void start(long durationMs) {
        stopAtMs = System.currentTimeMillis() + durationMs;
        if (getWidth() == 0) {
            post(this::initParticles);
        } else {
            initParticles();
        }
    }

    private void initParticles() {
        Random rnd = new Random();
        float density = getResources().getDisplayMetrics().density;
        float w = Math.max(getWidth(), 100);
        for (int i = 0; i < COUNT; i++) {
            x[i]      = rnd.nextFloat() * w;
            y[i]      = -rnd.nextFloat() * getHeight() * 0.6f;  // стартуют выше экрана
            vx[i]     = (rnd.nextFloat() - 0.5f) * 3f * density;
            vy[i]     = (1.5f + rnd.nextFloat() * 3.5f) * density;
            rot[i]    = rnd.nextFloat() * 360f;
            rotSpd[i] = (rnd.nextFloat() - 0.5f) * 6f;
            pw[i]     = (6 + rnd.nextFloat() * 10f) * density;
            ph[i]     = (3 + rnd.nextFloat() * 5f)  * density;
            color[i]  = PALETTE[rnd.nextInt(PALETTE.length)];
        }
        running = true;
        invalidate();
    }

    private void tick() {
        if (!running) return;
        if (System.currentTimeMillis() >= stopAtMs) {
            running = false;
            if (getParent() instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) getParent()).removeView(this);
            }
            return;
        }
        float height = Math.max(getHeight(), 100);
        float width  = Math.max(getWidth(), 100);
        for (int i = 0; i < COUNT; i++) {
            x[i]   += vx[i];
            y[i]   += vy[i];
            rot[i] += rotSpd[i];
            if (x[i] > width  + 20) x[i] = -20;
            if (x[i] < -20)         x[i] = width + 20;
            if (y[i] > height + 20) y[i] = -20;   // частица ушла вниз — перезапускаем сверху
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < COUNT; i++) {
            paint.setColor(color[i]);
            canvas.save();
            canvas.translate(x[i], y[i]);
            canvas.rotate(rot[i]);
            canvas.drawRect(-pw[i] / 2f, -ph[i] / 2f, pw[i] / 2f, ph[i] / 2f, paint);
            canvas.restore();
        }
        if (running) {
            removeCallbacks(tickRunnable);
            postDelayed(tickRunnable, 16L);
        }
    }
}
