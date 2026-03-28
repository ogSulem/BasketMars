package com.example.basketballgame;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.room.Room;

import com.example.basketballgame.data.AppDatabase;
import com.example.basketballgame.data.CloudLeaderboardRepository;
import com.example.basketballgame.data.LeaderboardRepository;
import com.google.firebase.FirebaseApp;

public class BasketballGameApp extends Application {

    private static final String TAG = "BasketballGameApp";

    private AppDatabase database;
    private LeaderboardRepository leaderboardRepository;

    private int startedActivities = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        // Инициализируем Firebase (graceful: без рабочего google-services.json выбросит исключение)
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized");
        } catch (Exception e) {
            Log.w(TAG, "Firebase init failed (placeholder google-services.json?): " + e.getMessage());
        }

        // Room — всегда доступен
        database = Room.databaseBuilder(this, AppDatabase.class, "basketball_leaderboard.db")
                .addMigrations(AppDatabase.MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build();

        leaderboardRepository = new LeaderboardRepository(
                database.leaderboardDao(),
                database.playerStatsDao());

        // Подключаем облачный лидерборд (будет работать только с реальным Firebase-проектом)
        CloudLeaderboardRepository cloudRepo = new CloudLeaderboardRepository();
        if (cloudRepo.isAvailable()) {
            leaderboardRepository.setCloudRepository(cloudRepo);
            Log.d(TAG, "Firestore cloud leaderboard attached");
        }

        leaderboardRepository.seedDemoDataIfEmpty();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(android.app.Activity a, Bundle b) { }

            @Override
            public void onActivityStarted(android.app.Activity activity) {
                startedActivities++;
                if (startedActivities == 1) MusicPlayer.onAppForeground(BasketballGameApp.this);
            }

            @Override public void onActivityResumed(android.app.Activity a) { }
            @Override public void onActivityPaused(android.app.Activity a) { }

            @Override
            public void onActivityStopped(android.app.Activity activity) {
                startedActivities = Math.max(0, startedActivities - 1);
                if (startedActivities == 0) MusicPlayer.onAppBackground();
            }

            @Override public void onActivitySaveInstanceState(android.app.Activity a, Bundle b) { }
            @Override public void onActivityDestroyed(android.app.Activity a) { }
        });
    }

    public LeaderboardRepository getLeaderboardRepository() {
        return leaderboardRepository;
    }
}
