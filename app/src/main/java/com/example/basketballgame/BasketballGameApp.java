package com.example.basketballgame;

import android.app.Application;
import android.os.Bundle;

import androidx.room.Room;

import com.example.basketballgame.data.AppDatabase;
import com.example.basketballgame.data.LeaderboardRepository;

public class BasketballGameApp extends Application {
    private AppDatabase database;
    private LeaderboardRepository leaderboardRepository;

    private int startedActivities = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "basketball_leaderboard.db"
        ).fallbackToDestructiveMigration().build();
        leaderboardRepository = new LeaderboardRepository(
                database.leaderboardDao(),
                database.playerStatsDao()
        );

        leaderboardRepository.seedDemoDataIfEmpty();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(android.app.Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(android.app.Activity activity) {
                startedActivities++;
                if (startedActivities == 1) {
                    MusicPlayer.onAppForeground(BasketballGameApp.this);
                }
            }

            @Override
            public void onActivityResumed(android.app.Activity activity) {
            }

            @Override
            public void onActivityPaused(android.app.Activity activity) {
            }

            @Override
            public void onActivityStopped(android.app.Activity activity) {
                startedActivities = Math.max(0, startedActivities - 1);
                if (startedActivities == 0) {
                    MusicPlayer.onAppBackground();
                }
            }

            @Override
            public void onActivitySaveInstanceState(android.app.Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(android.app.Activity activity) {
            }
        });
    }

    public LeaderboardRepository getLeaderboardRepository() {
        return leaderboardRepository;
    }
}
