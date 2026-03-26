package com.example.basketballgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;

public class InventoryActivity extends AppCompatActivity {
    private FrameLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new FrameLayout(this);

        SharedPreferences prefs = getSharedPreferences("basketball", Context.MODE_PRIVATE);
        int selectedBall = prefs.getInt("selectedBall", 0);
        int selectedHoop = prefs.getInt("selectedHoop", 0);
        int unlockedBall = prefs.getInt("unlockedBall", 0);
        int unlockedHoop = prefs.getInt("unlockedHoop", 0);
        int unlockedBg = prefs.getInt("unlockedBg", 0);
        int selectedBg = prefs.getInt("selectedBg", 0);
        int score = prefs.getInt("score", 0);

        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        root.setBackgroundResource(bgDrawables[selectedBg]);

        // Заголовок
        TextView title = new TextView(this);
        title.setText(R.string.inventory_title);
        title.setTextSize(32);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setShadowLayer(10, 0, 6, 0xFF8f5cff);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(32);
        root.addView(title, titleParams);

        // Кнопка назад
        ImageView backBtn = new ImageView(this);
        backBtn.setImageResource(R.drawable.ic_back);
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(140, 140);
        backParams.leftMargin = 32;
        backParams.topMargin = 48;
        backBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        root.addView(backBtn, backParams);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameView.animateButton(v);
                finish();
            }
        });

        // Контейнер секций
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerParams.topMargin = dp(100);
        containerParams.bottomMargin = dp(80);
        root.addView(container, containerParams);

        // Секция Мячи
        container.addView(makeInventorySection(
                getString(R.string.inventory_section_balls),
                SECTION_BALLS,
                new int[]{R.drawable.ball, R.drawable.ball2, R.drawable.ball3},
                new String[]{
                        getString(R.string.inventory_ball_default),
                        getString(R.string.inventory_ball_street),
                        getString(R.string.inventory_ball_legend)
                },
                selectedBall, unlockedBall, 3, new OnSelectListener() {
                    @Override
                    public void onSelect(int idx) {
                        prefs.edit().putInt("selectedBall", idx).apply();
                        recreate();
                    }
                }
        ));

        // Секция Кольца
        container.addView(makeInventorySection(
                getString(R.string.inventory_section_hoops),
                SECTION_HOOPS,
                new int[]{R.drawable.hoop, R.drawable.hoop2, R.drawable.hoop3},
                new String[]{
                        getString(R.string.inventory_hoop_default),
                        getString(R.string.inventory_hoop_street),
                        getString(R.string.inventory_hoop_legend)
                },
                selectedHoop, unlockedHoop, 3, new OnSelectListener() {
                    @Override
                    public void onSelect(int idx) {
                        prefs.edit().putInt("selectedHoop", idx).apply();
                        recreate();
                    }
                }
        ));

        // Секция Фоны
        container.addView(makeInventorySection(
                getString(R.string.inventory_section_backgrounds),
                SECTION_BACKGROUNDS,
                bgDrawables,
                new String[]{
                        getString(R.string.inventory_bg_pink),
                        getString(R.string.inventory_bg_blue),
                        getString(R.string.inventory_bg_orange)
                },
                selectedBg, unlockedBg, 3, new OnSelectListener() {
                    @Override
                    public void onSelect(int idx) {
                        prefs.edit().putInt("selectedBg", idx).apply();
                        getWindow().getDecorView().setBackgroundResource(bgDrawables[idx]);
                        root.setBackgroundResource(bgDrawables[idx]);
                        recreate();
                    }
                }
        ));

        // Счетчик очков внизу

        setContentView(root);
    }

    // Типы секций инвентаря — вместо сравнения строк
    private static final int SECTION_BALLS = 0;
    private static final int SECTION_HOOPS = 1;
    private static final int SECTION_BACKGROUNDS = 2;

    // Компактная секция инвентаря
    private LinearLayout makeInventorySection(String header, int sectionType, int[] drawables, String[] labels, int selected, int unlocked, int count, OnSelectListener onSelect) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, dp(8), 0, dp(8));
        // Заголовок
        TextView h = new TextView(this);
        h.setText(header);
        h.setTextSize(22);
        h.setTextColor(0xFF8f5cff);
        h.setGravity(Gravity.CENTER);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        h.setShadowLayer(6, 0, 3, 0xFF000000);
        LinearLayout.LayoutParams hParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        h.setLayoutParams(hParams);
        section.addView(h);
        // Иконки
        LinearLayout icons = new LinearLayout(this);
        icons.setOrientation(LinearLayout.HORIZONTAL);
        icons.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        icons.setLayoutParams(iconsParams);
        for (int i = 0; i < count; i++) {
            FrameLayout frame = new FrameLayout(this);
            GradientDrawable border = new GradientDrawable();
            border.setColor(0xFF23143A);
            border.setStroke(selected == i ? dp(6) : dp(4), 0xFF8f5cff);
            border.setCornerRadius(dp(18));
            frame.setBackground(border);
            frame.setTranslationZ(12);
            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(dp(80), dp(80));
            frame.setLayoutParams(frameParams);
            View icon;
            if (sectionType == SECTION_BACKGROUNDS) {
                icon = new View(this);
                icon.setBackgroundResource(drawables[i]);
            } else {
                ImageView img = new ImageView(this);
                if (sectionType == SECTION_BALLS) {
                    Bitmap b = GameView.renderBallPreview(this, i, dp(64));
                    img.setImageBitmap(b);
                } else if (sectionType == SECTION_HOOPS) {
                    Bitmap b = GameView.renderHoopPreview(this, i, dp(64));
                    img.setImageBitmap(b);
                } else {
                    img.setImageResource(drawables[i]);
                }
                img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                icon = img;
            }

            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(56), dp(56));
            iconParams.gravity = Gravity.CENTER;
            frame.addView(icon, iconParams);
            if (unlocked < i) {
                icon.setAlpha(0.3f);
                frame.setClickable(false);
                // Overlay
                View overlay = new View(this);
                GradientDrawable overlayBg = new GradientDrawable();
                overlayBg.setColor(0x883A2B5C);
                overlayBg.setCornerRadius(dp(18));
                overlay.setBackground(overlayBg);
                FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(dp(80), dp(80));
                overlayParams.gravity = Gravity.CENTER;
                overlay.setLayoutParams(overlayParams);
                frame.addView(overlay);
                // Диагональная линия
                View diag = new View(this) {
                    @Override
                    protected void onDraw(android.graphics.Canvas canvas) {
                        super.onDraw(canvas);
                        android.graphics.Paint paint = new android.graphics.Paint();
                        paint.setColor(0xFF8f5cff);
                        paint.setStrokeWidth(dp(4));
                        paint.setAlpha(180);
                        float pad = dp(8);
                        canvas.drawLine(pad, pad, getWidth() - pad, getHeight() - pad, paint);
                    }
                };
                FrameLayout.LayoutParams diagParams = new FrameLayout.LayoutParams(dp(80), dp(80));
                diagParams.gravity = Gravity.CENTER;
                diag.setLayoutParams(diagParams);
                frame.addView(diag);
            } else {
                frame.setClickable(true);
                final int idx = i;
                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GameView.animateButton(v);
                        onSelect.onSelect(idx);
                    }
                });
            }
            icons.addView(frame);
            if (i < count - 1) {
                View spacer = new View(this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(dp(18), 1);
                icons.addView(spacer, spacerParams);
            }
        }
        section.addView(icons);
        // Подписи
        LinearLayout labelsRow = new LinearLayout(this);
        labelsRow.setOrientation(LinearLayout.HORIZONTAL);
        labelsRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelsRow.setLayoutParams(labelsParams);
        labelsRow.setPadding(0, 0, 0, 0); // минимальный/отрицательный отступ
        for (int i = 0; i < count; i++) {
            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextColor(0xFFFFFFFF);
            label.setTextSize(13);
            label.setGravity(Gravity.CENTER);
            label.setTypeface(null, Typeface.BOLD);
            label.setShadowLayer(3, 0, 2, 0xCC000000);
            if (selected == i) {
                label.setPaintFlags(label.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            }
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(dp(80), LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.gravity = Gravity.CENTER;
            labelsRow.addView(label, labelParams);
            if (i < count - 1) {
                View spacer = new View(this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(dp(18), 1);
                labelsRow.addView(spacer, spacerParams);
            }
        }
        section.addView(labelsRow);
        return section;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Для передачи выбора
    private interface OnSelectListener {
        void onSelect(int idx);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("basketball", Context.MODE_PRIVATE);
        int bgIdx = prefs.getInt("selectedBg", 0);
        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        getWindow().getDecorView().setBackgroundResource(bgDrawables[bgIdx]);
        root.setBackgroundResource(bgDrawables[bgIdx]);
    }
}