package com.example.basketballgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Path;
import android.widget.ImageButton;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundResource(R.drawable.bg_gradient);

        // Заголовок
        final TextView title = new TextView(this);
        title.setText(R.string.achievements_title);
        title.setTextSize(36);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(12, 0, 8, 0xFF8f5cff);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = 64;
        root.addView(title, titleParams);

        // ScrollView для вертикального списка
        ScrollView scroll = new ScrollView(this);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        scrollParams.topMargin = 200;
        root.addView(scroll, scrollParams);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(list);

        // Achievement data from string resources (i18n-safe)
        String[] titles = getResources().getStringArray(R.array.achievement_titles);
        String[] descs  = getResources().getStringArray(R.array.achievement_descs);
        // Unlock thresholds must match GameView logic: score % 3 == 0 → levels 1..8 at scores 3,6,9,12,15,18,21,24
        int[] unlockScore = {3, 6, 9, 12, 15, 18, 21, 24};
        SharedPreferences prefs = getSharedPreferences("basketball", Context.MODE_PRIVATE);
        int achievementLevel = prefs.getInt("achievementLevel", 0);

        for (int i = 0; i < titles.length; i++) {
            FrameLayout card = new FrameLayout(this);
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(achievementLevel >= i + 1 ? 0xFF1A102B : 0xFF2D193C);
            cardBg.setStroke(achievementLevel >= i + 1 ? 16 : 10, 0xFF8f5cff);
            cardBg.setCornerRadius(32);
            card.setBackground(cardBg);
            FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(1200, 500);
            cardParams.topMargin = 80;
            card.setLayoutParams(cardParams);
            // Верх — название достижения
            TextView achTitle = new TextView(this);
            achTitle.setText(titles[i]);
            achTitle.setTextColor(achievementLevel >= i + 1 ? 0xFF8f5cff : 0xFFAAAAAA);
            achTitle.setTextSize(22);
            achTitle.setGravity(Gravity.CENTER_HORIZONTAL);
            achTitle.setShadowLayer(4, 0, 2, 0xFF8f5cff);
            achTitle.setPadding(0, 0, 0, 0);
            FrameLayout.LayoutParams achTitleParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            achTitleParams.topMargin = 36;
            card.addView(achTitle, achTitleParams);
            // Горизонтальная строка: число — награда — слово
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            FrameLayout.LayoutParams rowParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 400);
            rowParams.topMargin = 100;
            row.setLayoutParams(rowParams);
            // Число очков
            TextView pointsView = new TextView(this);
            pointsView.setText(String.valueOf(unlockScore[i]));
            pointsView.setTextColor(0xFF8f5cff);
            pointsView.setTextSize(32);
            pointsView.setGravity(Gravity.CENTER);
            pointsView.setShadowLayer(4, 0, 2, 0xFF8f5cff);
            LinearLayout.LayoutParams pointsParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            pointsParams.leftMargin = 40;
            row.addView(pointsView, pointsParams);
            // Награда (rewardCard) — всегда квадратная
            FrameLayout rewardCard = new FrameLayout(this);
            GradientDrawable rewardBorder = new GradientDrawable();
            rewardBorder.setColor(0x33000000);
            rewardBorder.setStroke(8, 0xFFB266FF);
            rewardBorder.setCornerRadius(24);
            rewardCard.setBackground(rewardBorder);
            LinearLayout.LayoutParams rewardCardParams = new LinearLayout.LayoutParams(180, 180);
            rewardCardParams.gravity = Gravity.CENTER_VERTICAL;
            rewardCard.setLayoutParams(rewardCardParams);
            int rewardRes = 0;
            String rewardLabel = "";
            boolean isBgReward = false;
            int bgPreviewRes = 0;
            if (i == 0) { rewardRes = R.drawable.ball2; rewardLabel = "Стрит"; }
            if (i == 1) { rewardRes = R.drawable.hoop2; rewardLabel = "Стрит (сетка)"; }
            if (i == 2) { rewardRes = R.drawable.ball3; rewardLabel = "Легенда"; }
            if (i == 3) { rewardRes = R.drawable.hoop3; rewardLabel = "Легенда (сетка)"; }
            if (i == 4) { rewardRes = R.drawable.ball; rewardLabel = "Все мячи"; }
            if (i == 5) { rewardRes = R.drawable.hoop; rewardLabel = "Все сетки"; }
            if (i == 6) { isBgReward = true; bgPreviewRes = R.drawable.bg_gradient2; rewardLabel = "Синий фон"; }
            if (i == 7) { isBgReward = true; bgPreviewRes = R.drawable.bg_gradient3; rewardLabel = "Оранжевый фон"; }
            if (rewardRes != 0) {
                ImageView rewardImg = new ImageView(this);
                // Используем процедурные превью для мячей/сеток, чтобы совпадало с игрой.
                if (rewardRes == R.drawable.ball2) {
                    Bitmap b = GameView.renderBallPreview(this, 1, 160);
                    rewardImg.setImageBitmap(b);
                } else if (rewardRes == R.drawable.ball3) {
                    Bitmap b = GameView.renderBallPreview(this, 2, 160);
                    rewardImg.setImageBitmap(b);
                } else if (rewardRes == R.drawable.ball) {
                    Bitmap b = GameView.renderBallPreview(this, 0, 160);
                    rewardImg.setImageBitmap(b);
                } else if (rewardRes == R.drawable.hoop2) {
                    Bitmap b = GameView.renderHoopPreview(this, 1, 160);
                    rewardImg.setImageBitmap(b);
                } else if (rewardRes == R.drawable.hoop3) {
                    Bitmap b = GameView.renderHoopPreview(this, 2, 160);
                    rewardImg.setImageBitmap(b);
                } else if (rewardRes == R.drawable.hoop) {
                    Bitmap b = GameView.renderHoopPreview(this, 0, 160);
                    rewardImg.setImageBitmap(b);
                } else {
                    rewardImg.setImageResource(rewardRes);
                }
                FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(160, 160);
                imgParams.gravity = Gravity.CENTER;
                rewardCard.addView(rewardImg, imgParams);
            } else if (isBgReward) {
                View preview = new View(this);
                preview.setBackgroundResource(bgPreviewRes);
                FrameLayout.LayoutParams previewParams = new FrameLayout.LayoutParams(160, 160);
                previewParams.gravity = Gravity.CENTER;
                preview.setLayoutParams(previewParams);
                rewardCard.addView(preview);
            }
            row.addView(rewardCard);
            // Слово 'очков'
            TextView wordView = new TextView(this);
            int pts = unlockScore[i];
            String word = "очков";
            if (pts % 10 == 1 && pts != 11) word = "очко";
            else if ((pts % 10 >= 2 && pts % 10 <= 4) && (pts < 10 || pts > 20)) word = "очка";
            wordView.setText(word);
            wordView.setTextColor(0xFF8f5cff);
            wordView.setTextSize(22);
            wordView.setGravity(Gravity.CENTER);
            wordView.setShadowLayer(3, 0, 1, 0xFF8f5cff);
            LinearLayout.LayoutParams wordParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            wordParams.rightMargin = 40;
            row.addView(wordView, wordParams);
            card.addView(row);
            // Overlay если не открыто (на всю карточку)
            if (achievementLevel < i + 1) {
                // Overlay на всю карточку (чуть светлее)
                View overlay = new View(this);
                overlay.setBackgroundColor(0x77_000000);
                FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                overlay.setLayoutParams(overlayParams);
                card.addView(overlay);
                // Диагональная линия на всю карточку
                View diag = new View(this) {
                    @Override
                    protected void onDraw(android.graphics.Canvas canvas) {
                        super.onDraw(canvas);
                        android.graphics.Paint paint = new android.graphics.Paint();
                        paint.setColor(0xCC8f5cff);
                        paint.setStrokeWidth(14);
                        paint.setAlpha(180);
                        canvas.drawLine(1, 1, getWidth() - 1, getHeight() - 1, paint);
                    }
                };
                FrameLayout.LayoutParams diagParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                diag.setLayoutParams(diagParams);
                card.addView(diag);
            }
            list.addView(card);
        }

        // Кнопка назад (стрелка)
        ImageView backBtn = new ImageView(this);
        backBtn.setImageResource(R.drawable.ic_back);
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(140, 140);
        backParams.leftMargin = 32;
        backParams.topMargin = 48;
        root.addView(backBtn, backParams);
        backBtn.setOnClickListener(v -> {
            GameView.animateButton(v);
            finish();
        });

        setContentView(root);
        // Делаю заголовок всегда поверх
        title.bringToFront();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("basketball", Context.MODE_PRIVATE);
        int bgIdx = prefs.getInt("selectedBg", 0);
        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        getWindow().getDecorView().setBackgroundResource(bgDrawables[bgIdx]);
    }

    private ShapeDrawable drawUnlockedIcon() {
        Path path = new Path();
        // Дуга (дуга замка)
        path.addArc(15, 20, 65, 70, 200, 140);
        // Корпус
        path.moveTo(25, 55); path.lineTo(25, 65); path.lineTo(55, 65); path.lineTo(55, 55);
        path.close();
        // Замочная скважина
        path.moveTo(40, 60); path.lineTo(40, 63);
        path.addCircle(40, 58, 3, Path.Direction.CW);
        ShapeDrawable d = new ShapeDrawable(new PathShape(path, 80, 80));
        d.getPaint().setColor(0xFF8f5cff);
        d.getPaint().setStyle(android.graphics.Paint.Style.STROKE);
        d.getPaint().setStrokeWidth(6);
        return d;
    }

    private ShapeDrawable drawLockIcon() {
        Path path = new Path();
        path.addArc(25, 30, 85, 90, 200, 140);
        path.moveTo(40, 70); path.lineTo(40, 90); path.lineTo(70, 90); path.lineTo(70, 70);
        path.close();
        path.moveTo(55, 80); path.lineTo(55, 87);
        path.addCircle(55, 77, 5, Path.Direction.CW);
        ShapeDrawable d = new ShapeDrawable(new PathShape(path, 80, 80));
        d.getPaint().setColor(0xFF8f5cff);
        d.getPaint().setStyle(android.graphics.Paint.Style.STROKE);
        d.getPaint().setStrokeWidth(7);
        return d;
    }
} 