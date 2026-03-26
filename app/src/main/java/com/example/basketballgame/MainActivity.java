package com.example.basketballgame;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.pm.ApplicationInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

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
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameView.animateButton(v);
                showModeDialog();
            }
        });

        Button leaderboardButton = findViewById(R.id.leaderboard_button);
        leaderboardButton.setBackgroundResource(R.drawable.btn_rounded_dark);
        leaderboardButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        Button selectBallButton = findViewById(R.id.select_ball_button);
        selectBallButton.setBackgroundResource(R.drawable.btn_rounded_dark);
        selectBallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameView.animateButton(v);
                Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
                startActivity(intent);
            }
        });

        Button achievementsButton = findViewById(R.id.achievements_button);
        achievementsButton.setBackgroundResource(R.drawable.btn_rounded_dark);
        achievementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameView.animateButton(v);
                Intent intent = new Intent(MainActivity.this, AchievementsActivity.class);
                startActivity(intent);
            }
        });

        Button settingsButton = findViewById(R.id.settings_button);
        if (settingsButton != null) {
            settingsButton.setBackgroundResource(R.drawable.btn_rounded_dark);
            settingsButton.setOnClickListener(v -> {
                GameView.animateButton(v);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showModeDialog() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setOnShowListener(d -> {
            View bottomSheet = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        View content = getLayoutInflater().inflate(R.layout.bottomsheet_mode_select, null);

        View arcade = content.findViewById(R.id.btn_arcade);
        View timed = content.findViewById(R.id.btn_timed);
        View duel = content.findViewById(R.id.btn_duel);

        arcade.setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.ARCADE, null);
        });

        timed.setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.TIMED, null);
        });

        duel.setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.ONLINE_DUEL, GameActivity.DUEL_TYPE_BOT);
        });

        sheet.setContentView(content);
        if (sheet.getWindow() != null) {
            sheet.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        sheet.show();
    }

    private void showDuelTypeDialog() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setOnShowListener(d -> {
            View bottomSheet = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        View content = getLayoutInflater().inflate(R.layout.bottomsheet_duel_type, null);

        View bot = content.findViewById(R.id.card_duel_bot);
        View online = content.findViewById(R.id.card_duel_online);

        bot.setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.ONLINE_DUEL, GameActivity.DUEL_TYPE_BOT);
        });

        online.setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            startGame(GameMode.ONLINE_DUEL, GameActivity.DUEL_TYPE_ONLINE);
        });

        sheet.setContentView(content);
        if (sheet.getWindow() != null) {
            sheet.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        sheet.show();
    }

    private void startGame(GameMode mode, String duelType) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(GameMode.EXTRA_KEY, mode.name());
        if (mode == GameMode.ONLINE_DUEL && duelType != null) {
            intent.putExtra(GameActivity.EXTRA_DUEL_TYPE, duelType);
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.content.SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        int bgIdx = prefs.getInt("selectedBg", 0);
        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        LinearLayout root = findViewById(R.id.root_layout);
        if (root != null) {
            root.setBackgroundResource(bgDrawables[bgIdx]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}