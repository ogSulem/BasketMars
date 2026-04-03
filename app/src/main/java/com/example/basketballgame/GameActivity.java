package com.example.basketballgame;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.basketballgame.data.LeaderboardRepository;
import com.example.basketballgame.data.PlayerStats;
import com.example.basketballgame.net.DemoDuelClient;
import com.example.basketballgame.net.MatchClient;
import com.example.basketballgame.net.OnlinePvpClient;

public class GameActivity extends AppCompatActivity {

    private ImageButton playBtn;
    private MatchClient matchClient;
    private GameView gameView;
    private GameMode mode = GameMode.ARCADE;
    private LeaderboardRepository leaderboardRepository;
    private String playerName;
    private boolean arcadeSaved = false;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundResource(R.drawable.bg_gradient);

        mode = GameMode.fromName(getIntent().getStringExtra(GameMode.EXTRA_KEY));
        gameView = new GameView(this, mode);
        gameView.setSessionListener(this::handleSessionComplete);
        root.addView(gameView);

        // Кнопка "Назад"
        ImageView backButton = new ImageView(this);
        backButton.setImageResource(R.drawable.ic_back);
        backButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        backButton.setContentDescription(getString(R.string.btn_back));
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(140, 140);
        backParams.leftMargin = 32;
        backParams.topMargin = 48;
        root.addView(backButton, backParams);
        backButton.setOnClickListener(v -> {
            GameView.animateButton(v);
            maybeSaveArcadeRun();
            finish();
        });

        // Панель управления музыкой (по центру сверху)
        View musicControls = getLayoutInflater().inflate(R.layout.music_controls, root, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        musicControls.setLayoutParams(params);
        root.addView(musicControls);
        musicControls.findViewById(R.id.music_prev).setOnClickListener(v -> MusicPlayer.prev(this));
        musicControls.findViewById(R.id.music_next).setOnClickListener(v -> MusicPlayer.next(this));
        playBtn = musicControls.findViewById(R.id.music_play);
        playBtn.setColorFilter(MusicPlayer.isPlaying() ? 0xFFFFFFFF : 0xFF444444);
        playBtn.setOnClickListener(v -> {
            MusicPlayer.toggle(this);
            playBtn.setColorFilter(MusicPlayer.isPlaying() ? 0xFFFFFFFF : 0xFF444444);
        });

        setContentView(root);

        leaderboardRepository = ((BasketballGameApp) getApplication()).getLeaderboardRepository();
        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        playerName = prefs.getString("playerName", getString(R.string.default_player_name));

        if (mode == GameMode.ONLINE_DUEL) {
            initBotClient();
        } else if (mode == GameMode.ONLINE_PVP) {
            String roomId = getIntent().getStringExtra(MatchmakingActivity.EXTRA_ROOM_ID);
            int myRole = getIntent().getIntExtra(MatchmakingActivity.EXTRA_MY_ROLE, 1);
            if (roomId != null) {
                initOnlinePvpClient(roomId, myRole);
            } else {
                // Нет данных матча — откат к боту
                initBotClient();
            }
        }
    }

    private void updateMusicNoteColor() {
        if (playBtn != null) {
            playBtn.setColorFilter(MusicPlayer.isPlaying() ? 0xFFFFFFFF : 0xFF444444);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        int bgIdx = prefs.getInt("selectedBg", 0);
        int[] bgDrawables = {R.drawable.bg_gradient, R.drawable.bg_gradient2, R.drawable.bg_gradient3};
        getWindow().getDecorView().setBackgroundResource(bgDrawables[bgIdx]);
        MusicPlayer.ensureState(this);
        updateMusicNoteColor();

        if (mode == GameMode.ONLINE_DUEL && matchClient == null && gameView != null && !gameView.isGameOver()) {
            initBotClient();
        } else if (mode == GameMode.ONLINE_PVP && matchClient == null && gameView != null && !gameView.isGameOver()) {
            String roomId = getIntent().getStringExtra(MatchmakingActivity.EXTRA_ROOM_ID);
            int myRole = getIntent().getIntExtra(MatchmakingActivity.EXTRA_MY_ROLE, 1);
            if (roomId != null) initOnlinePvpClient(roomId, myRole);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        maybeSaveArcadeRun();
        if (matchClient != null) {
            matchClient.disconnect();
            matchClient = null;
        }
    }

    private void handleSessionComplete(GameMode mode, int playerScore, int rivalScore) {
        if (leaderboardRepository == null) return;
        String userId = AuthManager.getInstance(this).getUserId();
        leaderboardRepository.saveScore(mode.name(), playerScore, playerName, userId);
        leaderboardRepository.updateStats(
                stats -> applyStats(stats, mode, playerScore, rivalScore),
                () -> runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.result_saved), Toast.LENGTH_SHORT).show();
                    if (mode != GameMode.ARCADE) {
                        showResultsDialog(mode, playerScore, rivalScore);
                    }
                }));
    }

    private void showResultsDialog(GameMode mode, int playerScore, int rivalScore) {
        if (gameView != null) gameView.setPaused(true);

        if (matchClient != null) {
            try { matchClient.disconnect(); } catch (Exception ignored) { }
            matchClient = null;
            if (gameView != null) gameView.setMatchClient(null);
        }

        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setOnShowListener(d -> {
            View bottomSheet = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) bottomSheet.setBackgroundColor(Color.TRANSPARENT);
        });
        View content = getLayoutInflater().inflate(R.layout.bottomsheet_match_results, null);
        sheet.setContentView(content);
        if (sheet.getWindow() != null) {
            sheet.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView title = content.findViewById(R.id.result_title);
        TextView playerScoreText = content.findViewById(R.id.player_score_value);
        TextView rivalScoreText = content.findViewById(R.id.rival_score_value);
        TextView rivalScoreLabel = content.findViewById(R.id.rival_score_label);
        View rivalContainer = content.findViewById(R.id.rival_score_container);
        View scoreDivider = content.findViewById(R.id.score_divider);

        playerScoreText.setText(String.valueOf(playerScore));

        if (mode == GameMode.TIMED) {
            title.setText(getString(R.string.result_time_over));
            rivalContainer.setVisibility(View.GONE);
            if (scoreDivider != null) scoreDivider.setVisibility(View.GONE);
            showConfetti();
        } else {
            rivalScoreText.setText(String.valueOf(rivalScore));
            // Set rival label: opponent name for PvP, "БОТ" for duel
            if (rivalScoreLabel != null) {
                if (mode == GameMode.ONLINE_PVP) {
                    String opponentName = getIntent().getStringExtra(MatchmakingActivity.EXTRA_OPPONENT_NAME);
                    rivalScoreLabel.setText(opponentName != null && !opponentName.isEmpty()
                            ? opponentName.toUpperCase()
                            : getString(R.string.result_rival_label_online));
                } else {
                    rivalScoreLabel.setText(getString(R.string.result_rival_label_bot));
                }
            }
            if (playerScore > rivalScore) {
                title.setText(getString(R.string.result_win));
                title.setTextColor(0xFF4CAF50);
                showConfetti();
            } else if (playerScore < rivalScore) {
                title.setText(getString(R.string.result_lose));
                title.setTextColor(0xFFF44336);
            } else {
                title.setText(getString(R.string.result_draw));
            }
        }

        content.findViewById(R.id.btn_restart).setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            if (mode == GameMode.ONLINE_PVP) {
                // Для онлайн-режима переходим к матчмейкингу, а не в игру напрямую
                startActivity(new Intent(GameActivity.this, MatchmakingActivity.class));
            } else {
                Intent intent = new Intent(GameActivity.this, GameActivity.class);
                intent.putExtra(GameMode.EXTRA_KEY, mode.name());
                startActivity(intent);
            }
            finish();
        });

        content.findViewById(R.id.btn_menu).setOnClickListener(v -> {
            GameView.animateButton(v);
            sheet.dismiss();
            finish();
        });

        sheet.setCancelable(false);
        sheet.show();
    }

    /** Запускает анимацию конфетти поверх всего контента активити на 4 секунды. */
    private void showConfetti() {
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        ConfettiView confetti = new ConfettiView(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        decor.addView(confetti, lp);
        confetti.start(4000);
    }

    private void maybeSaveArcadeRun() {
        if (arcadeSaved) return;
        if (mode != GameMode.ARCADE) return;
        if (leaderboardRepository == null || gameView == null) return;

        int score = gameView.getScore();
        if (score <= 0) return;

        arcadeSaved = true;
        String userId = AuthManager.getInstance(this).getUserId();
        leaderboardRepository.saveScore(GameMode.ARCADE.name(), score, playerName, userId);
        leaderboardRepository.updateStats(stats -> applyStats(stats, GameMode.ARCADE, score, 0), null);
    }

    private void applyStats(PlayerStats stats, GameMode mode, int playerScore, int rivalScore) {
        stats.totalGames++;
        switch (mode) {
            case ARCADE:
                if (playerScore > stats.arcadeBest) stats.arcadeBest = playerScore;
                break;
            case TIMED:
                if (playerScore > stats.timedBest) stats.timedBest = playerScore;
                break;
            case ONLINE_DUEL:
                if (playerScore > stats.duelBest) stats.duelBest = playerScore;
                if (playerScore >= rivalScore) stats.duelWins++;
                else stats.duelLosses++;
                break;
            case ONLINE_PVP:
                if (playerScore > stats.onlinePvpBest) stats.onlinePvpBest = playerScore;
                if (playerScore >= rivalScore) stats.onlinePvpWins++;
                else stats.onlinePvpLosses++;
                break;
        }
    }

    /** Инициализация бота (единственный вариант дуэли). */
    private void initBotClient() {        MatchClient.Listener listener = new MatchClient.Listener() {
            @Override public void onConnected() { }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    if (gameView != null && !gameView.isGameOver()) {
                        gameView.updateRemoteScore(0);
                    }
                });
            }

            @Override public void onError(String message, Throwable throwable) { }

            @Override
            public void onGhostSnapshot(float x, float y, boolean moving, int remoteScore) {
                gameView.applyGhostSnapshot(x, y, moving);
                gameView.updateRemoteScore(remoteScore);
            }
        };

        matchClient = new DemoDuelClient(listener);
        gameView.setMatchClient(matchClient);
        gameView.setDeferOpponentStart(false);
        gameView.setOpponentConnectTarget("demo");
        matchClient.connect("demo");
    }

    /** Инициализация реального онлайн-клиента для режима ONLINE_PVP. */
    private void initOnlinePvpClient(String roomId, int myRole) {
        MatchClient.Listener listener = new MatchClient.Listener() {
            @Override public void onConnected() { }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    if (gameView != null && !gameView.isGameOver()) {
                        gameView.updateRemoteScore(0);
                    }
                });
            }

            @Override public void onError(String message, Throwable throwable) {
                runOnUiThread(() -> Toast.makeText(GameActivity.this,
                        getString(R.string.error_pvp_connection), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onGhostSnapshot(float x, float y, boolean moving, int remoteScore) {
                if (gameView != null) {
                    gameView.applyGhostSnapshot(x, y, moving);
                    gameView.updateRemoteScore(remoteScore);
                }
            }
        };

        matchClient = new OnlinePvpClient(roomId, myRole, listener);
        gameView.setMatchClient(matchClient);
        gameView.setDeferOpponentStart(false);
        gameView.setOpponentConnectTarget(roomId);
        matchClient.connect(roomId);
    }
}
