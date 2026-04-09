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

    private FrameLayout root;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        this.root = root;

        SharedPreferences prefs = getSharedPreferences("basketball", Context.MODE_PRIVATE);
        int selectedBg = prefs.getInt("selectedBg", 0);
        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        root.setBackgroundResource(bgDrawables[selectedBg]);

        // Заголовок
        final TextView title = new TextView(this);
        title.setText(R.string.achievements_title);
        title.setTextSize(36);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(12, 0, 8, 0xFF8f5cff);
        title.setPadding(dp(68), 0, dp(16), 0);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(20);
        root.addView(title, titleParams);

        // ScrollView для вертикального списка
        ScrollView scroll = new ScrollView(this);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        scrollParams.topMargin = dp(80);
        root.addView(scroll, scrollParams);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setGravity(Gravity.CENTER_HORIZONTAL);
        list.setPadding(dp(8), 0, dp(8), dp(16));
        scroll.addView(list);

        // Achievement data from string resources (i18n-safe)
        String[] titles = getResources().getStringArray(R.array.achievement_titles);
        String[] descs  = getResources().getStringArray(R.array.achievement_descs);
        // Unlock thresholds must match GameView logic: levels 1..6 at scores 5,10,15,20,25,30
        int[] unlockScore = {5, 10, 15, 20, 25, 30};
        int achievementLevel = prefs.getInt("achievementLevel", 0);

        for (int i = 0; i < titles.length; i++) {
            boolean unlocked = achievementLevel >= i + 1;

            // Карточка достижения
            FrameLayout card = new FrameLayout(this);
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(unlocked ? 0xFF1A102B : 0xFF2D193C);
            cardBg.setStroke(dp(unlocked ? 4 : 2), unlocked ? 0xFF8f5cff : 0xFF5a3a7a);
            cardBg.setCornerRadius(dp(14));
            card.setBackground(cardBg);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.topMargin = dp(12);
            card.setLayoutParams(cardParams);

            // Вертикальный контейнер внутри карточки — исключает наложение элементов
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.addView(cardContent, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

            // Название достижения
            TextView achTitle = new TextView(this);
            achTitle.setText(titles[i]);
            achTitle.setTextColor(unlocked ? 0xFF8f5cff : 0xFFAAAAAA);
            achTitle.setTextSize(20);
            achTitle.setGravity(Gravity.CENTER_HORIZONTAL);
            achTitle.setShadowLayer(4, 0, 2, 0xFF8f5cff);
            cardContent.addView(achTitle, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // Разделитель
            View divider = new View(this);
            LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
            divParams.topMargin = dp(8);
            divParams.bottomMargin = dp(8);
            divider.setBackgroundColor(unlocked ? 0x558f5cff : 0x33ffffff);
            cardContent.addView(divider, divParams);

            // Горизонтальная строка: число — награда — слово
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            cardContent.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(90)));

            // Число очков (слева)
            LinearLayout leftCol = new LinearLayout(this);
            leftCol.setOrientation(LinearLayout.VERTICAL);
            leftCol.setGravity(Gravity.CENTER);
            leftCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            TextView pointsView = new TextView(this);
            pointsView.setText(String.valueOf(unlockScore[i]));
            pointsView.setTextColor(unlocked ? 0xFF8f5cff : 0xFF888888);
            pointsView.setTextSize(34);
            pointsView.setGravity(Gravity.CENTER);
            pointsView.setShadowLayer(4, 0, 2, 0xFF8f5cff);
            leftCol.addView(pointsView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            int pts = unlockScore[i];
            TextView wordView = new TextView(this);
            wordView.setText(getResources().getQuantityString(R.plurals.achievement_points, pts));
            wordView.setTextColor(unlocked ? 0xFFB266FF : 0xFF666666);
            wordView.setTextSize(13);
            wordView.setGravity(Gravity.CENTER);
            leftCol.addView(wordView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.addView(leftCol);

            // Награда (по центру) — всегда квадратная
            FrameLayout rewardCard = new FrameLayout(this);
            GradientDrawable rewardBorder = new GradientDrawable();
            rewardBorder.setColor(unlocked ? 0x44000000 : 0x22000000);
            rewardBorder.setStroke(dp(unlocked ? 3 : 2), unlocked ? 0xFFB266FF : 0xFF5a3a7a);
            rewardBorder.setCornerRadius(dp(10));
            rewardCard.setBackground(rewardBorder);
            int rewardSizePx = dp(80);
            LinearLayout.LayoutParams rewardCardParams = new LinearLayout.LayoutParams(rewardSizePx, rewardSizePx);
            rewardCardParams.gravity = Gravity.CENTER_VERTICAL;
            rewardCard.setLayoutParams(rewardCardParams);
            int rewardRes = 0;
            boolean isBgReward = false;
            int bgPreviewRes = 0;
            if (i == 0) { rewardRes = R.drawable.ball2; }
            if (i == 1) { rewardRes = R.drawable.hoop2; }
            if (i == 2) { isBgReward = true; bgPreviewRes = R.drawable.bg_gradient2; }
            if (i == 3) { rewardRes = R.drawable.ball3; }
            if (i == 4) { rewardRes = R.drawable.hoop3; }
            if (i == 5) { isBgReward = true; bgPreviewRes = R.drawable.bg_gradient3; }
            if (rewardRes != 0) {
                ImageView rewardImg = new ImageView(this);
                rewardImg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                Bitmap b = null;
                if (rewardRes == R.drawable.ball2)  b = GameView.renderBallPreview(this, 1, rewardSizePx);
                else if (rewardRes == R.drawable.ball3) b = GameView.renderBallPreview(this, 2, rewardSizePx);
                else if (rewardRes == R.drawable.ball)  b = GameView.renderBallPreview(this, 0, rewardSizePx);
                else if (rewardRes == R.drawable.hoop2) b = GameView.renderHoopPreview(this, 1, rewardSizePx);
                else if (rewardRes == R.drawable.hoop3) b = GameView.renderHoopPreview(this, 2, rewardSizePx);
                else if (rewardRes == R.drawable.hoop)  b = GameView.renderHoopPreview(this, 0, rewardSizePx);
                if (b != null) rewardImg.setImageBitmap(b);
                else rewardImg.setImageResource(rewardRes);
                FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(rewardSizePx, rewardSizePx);
                imgParams.gravity = Gravity.CENTER;
                rewardCard.addView(rewardImg, imgParams);
            } else if (isBgReward) {
                View preview = new View(this);
                preview.setBackgroundResource(bgPreviewRes);
                GradientDrawable roundClip = new GradientDrawable();
                roundClip.setCornerRadius(dp(8));
                FrameLayout.LayoutParams previewParams = new FrameLayout.LayoutParams(rewardSizePx, rewardSizePx);
                previewParams.gravity = Gravity.CENTER;
                rewardCard.addView(preview, previewParams);
            }
            if (!unlocked) rewardCard.setAlpha(0.4f);
            row.addView(rewardCard);

            // Описание (справа)
            LinearLayout rightCol = new LinearLayout(this);
            rightCol.setOrientation(LinearLayout.VERTICAL);
            rightCol.setGravity(Gravity.CENTER);
            rightCol.setPadding(dp(10), 0, 0, 0);
            rightCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            TextView descView = new TextView(this);
            descView.setText(descs[i]);
            descView.setTextColor(unlocked ? 0xFFDDDDDD : 0xFF666666);
            descView.setTextSize(13);
            descView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            rightCol.addView(descView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.addView(rightCol);

            // Overlay если не открыто (на всю карточку поверх контента)
            if (!unlocked) {
                View overlay = new View(this);
                overlay.setBackgroundColor(0x55_000000);
                card.addView(overlay, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                View diag = new View(this) {
                    @Override
                    protected void onDraw(android.graphics.Canvas canvas) {
                        super.onDraw(canvas);
                        android.graphics.Paint paint = new android.graphics.Paint();
                        paint.setColor(0xCC8f5cff);
                        paint.setStrokeWidth(dp(3));
                        paint.setAlpha(140);
                        canvas.drawLine(dp(12), dp(12), getWidth() - dp(12), getHeight() - dp(12), paint);
                    }
                };
                card.addView(diag, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            }
            list.addView(card);
        }

        // Кнопка назад (стрелка)
        ImageView backBtn = new ImageView(this);
        backBtn.setImageResource(R.drawable.ic_back);
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(dp(56), dp(56));
        backParams.leftMargin = dp(12);
        backParams.topMargin = dp(14);
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
        if (root != null) root.setBackgroundResource(bgDrawables[bgIdx]);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
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