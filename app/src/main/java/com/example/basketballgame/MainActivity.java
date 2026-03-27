package com.example.basketballgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.ApplicationInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean debuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (debuggable) {
            Toast.makeText(this, "DEBUG BUILD", Toast.LENGTH_LONG).show();
        }

        Button playButton = findViewById(R.id.play_button);
        playButton.setBackgroundResource(R.drawable.btn_rounded_dark);
        playButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            showModeDialog();
        });

        Button leaderboardButton = findViewById(R.id.leaderboard_button);
        leaderboardButton.setBackgroundResource(R.drawable.btn_rounded_dark);
        leaderboardButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
        });

        Button selectBallButton = findViewById(R.id.select_ball_button);
        selectBallButton.setBackgroundResource(R.drawable.btn_rounded_dark);
        selectBallButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            startActivity(new Intent(MainActivity.this, InventoryActivity.class));
        });

        Button achievementsButton = findViewById(R.id.achievements_button);
        achievementsButton.setBackgroundResource(R.drawable.btn_rounded_dark);
        achievementsButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
        });

        Button settingsButton = findViewById(R.id.settings_button);
        if (settingsButton != null) {
            settingsButton.setBackgroundResource(R.drawable.btn_rounded_dark);
            settingsButton.setOnClickListener(v -> {
                GameView.animateButton(v);
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            });
        }
    }

    private void showModeDialog() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setOnShowListener(d -> {
            View bottomSheet = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) bottomSheet.setBackgroundColor(Color.TRANSPARENT);
        });
        View content = getLayoutInflater().inflate(R.layout.bottomsheet_mode_select, null);

        content.findViewById(R.id.btn_arcade).setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.ARCADE);
        });

        content.findViewById(R.id.btn_timed).setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.TIMED);
        });

        content.findViewById(R.id.btn_duel).setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.ONLINE_DUEL);
        });

        sheet.setContentView(content);
        if (sheet.getWindow() != null) {
            sheet.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        sheet.show();
    }

    private void startGame(GameMode mode) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(GameMode.EXTRA_KEY, mode.name());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        int bgIdx = prefs.getInt("selectedBg", 0);
        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        LinearLayout root = findViewById(R.id.root_layout);
        if (root != null) root.setBackgroundResource(bgDrawables[bgIdx]);

        // Показываем имя аккаунта Google, если вошли
        TextView authorText = null;
        // (поиск по корневому layout'у)
        View v = root != null ? root.findViewWithTag("account_label") : null;
        if (v instanceof TextView) {
            AuthManager auth = AuthManager.getInstance(this);
            if (auth.isSignedIn()) {
                ((TextView) v).setText(auth.getDisplayName());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
