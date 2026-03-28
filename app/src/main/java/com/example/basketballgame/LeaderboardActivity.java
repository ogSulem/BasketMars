package com.example.basketballgame;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballgame.data.LeaderboardRepository;
import com.example.basketballgame.data.PlayerStats;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {
    private LeaderboardAdapter adapter;
    private LeaderboardRepository repository;
    private ProgressBar progressBar;
    private TabLayout tabs;
    private TextView statsArcade;
    private TextView statsTimed;
    private TextView statsDuel;
    private TextView statsOnlinePvp;
    private TextView emptyState;
    private String selectedMode = GameMode.ARCADE.name();

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        View backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                GameView.animateButton(v);
                finish();
            });
        }

        RecyclerView recyclerView = findViewById(R.id.leaderboard_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.loading_spinner);
        tabs = findViewById(R.id.mode_tabs);
        statsArcade = findViewById(R.id.stats_arcade);
        statsTimed = findViewById(R.id.stats_timed);
        statsDuel = findViewById(R.id.stats_duel);
        statsOnlinePvp = findViewById(R.id.stats_online_pvp);
        emptyState = findViewById(R.id.empty_state);

        repository = ((BasketballGameApp) getApplication()).getLeaderboardRepository();

        setupTabs();
        repository.seedDemoDataIfEmpty(() -> runOnUiThread(() -> {
            loadStats();
            loadTopScores(selectedMode);
        }));
    }

    private void setupTabs() {
        tabs.removeAllTabs();
        tabs.addTab(tabs.newTab().setText(getString(R.string.leaderboard_tab_arcade)), true);
        tabs.addTab(tabs.newTab().setText(getString(R.string.leaderboard_tab_timed)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.leaderboard_tab_duel)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.leaderboard_tab_online)));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == 0) {
                    selectedMode = GameMode.ARCADE.name();
                } else if (pos == 1) {
                    selectedMode = GameMode.TIMED.name();
                } else if (pos == 2) {
                    selectedMode = GameMode.ONLINE_DUEL.name();
                } else {
                    selectedMode = GameMode.ONLINE_PVP.name();
                }
                loadTopScores(selectedMode);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                loadTopScores(selectedMode);
            }
        });
    }

    private void loadTopScores(String mode) {
        progressBar.setVisibility(View.VISIBLE);
        repository.getTopScores(mode, 20, entries -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            adapter.update(entries);

            if (emptyState != null) {
                emptyState.setVisibility(entries == null || entries.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }));
    }

    private void loadStats() {
        repository.getStats(stats -> runOnUiThread(() -> bindStats(stats)));
    }

    private void bindStats(PlayerStats stats) {
        if (statsArcade != null) statsArcade.setText(String.valueOf(stats.arcadeBest));
        if (statsTimed != null) statsTimed.setText(String.valueOf(stats.timedBest));
        if (statsDuel != null) statsDuel.setText(String.valueOf(stats.duelBest));
        if (statsOnlinePvp != null) statsOnlinePvp.setText(String.valueOf(stats.onlinePvpBest));
    }
}
