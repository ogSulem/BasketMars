package com.example.basketballgame;

import android.content.Context;
import android.media.MediaPlayer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.widget.Toast;
import android.content.SharedPreferences;

public class MusicPlayer {
    private static MediaPlayer player = null;
    private static List<Integer> tracks = new ArrayList<>();
    private static int current = 0;

    public static void onAppForeground(Context context) {
        ensureState(context);
    }

    public static void onAppBackground() {
        pause();
    }

    public static void play(final Context context) {
        if (player != null) return;
        final Context appContext = context.getApplicationContext();
        // Найти все mp3 в res/raw
        if (tracks.isEmpty()) {
            StringBuilder found = new StringBuilder();
            try {
                Field[] fields = Class.forName(appContext.getPackageName() + ".R$raw").getFields();
                for (Field f : fields) {
                    String name = f.getName();
                    int resId = appContext.getResources().getIdentifier(name, "raw", appContext.getPackageName());
                    if (resId != 0) {
                        tracks.add(resId);
                        found.append(name).append(" ");
                    }
                }
            } catch (Exception e) { Log.e("MusicPlayer", "Ошибка поиска mp3", e); }
            if (tracks.isEmpty()) {
                Log.e("MusicPlayer", "Не найдено ни одного mp3 в res/raw");
                Toast.makeText(appContext, "Нет музыки в папке res/raw!", Toast.LENGTH_LONG).show();
                return;
            } else {
                Log.i("MusicPlayer", "Найдены треки: " + found);
                // Toast.makeText(context, "Музыка: " + found, Toast.LENGTH_LONG).show();
            }
        }
        startTrack(appContext, 0);
    }

    private static void startTrack(final Context context, int idx) {
        stop();
        current = idx;
        player = MediaPlayer.create(context, tracks.get(current));
        if (player == null) return;
        player.setOnCompletionListener(mp -> {
            int next = (current + 1) % tracks.size();
            startTrack(context, next);
        });
        player.setLooping(tracks.size() == 1); // если один трек — зацикливаем
        player.start();
    }

    public static void stop() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    public static void pause() {
        if (player != null && player.isPlaying()) player.pause();
    }

    public static void resume() {
        if (player != null && !player.isPlaying()) player.start();
    }

    public static boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public static void toggle(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("basketball", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("musicEnabled", true);
        if (enabled) {
            pause();
            prefs.edit().putBoolean("musicEnabled", false).apply();
        } else {
            play(context);
            resume();
            prefs.edit().putBoolean("musicEnabled", true).apply();
        }
    }

    public static void ensureState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("basketball", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("musicEnabled", true);
        if (enabled) {
            play(context);
            resume();
        } else {
            pause();
        }
    }

    public static void next(Context context) {
        if (tracks.isEmpty()) play(context);
        if (tracks.size() > 1) {
            current = (current + 1) % tracks.size();
            SharedPreferences prefs = context.getSharedPreferences("basketball", Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean("musicEnabled", true);
            if (enabled) {
                startTrack(context, current);
            } else {
                stop();
            }
        }
    }

    public static void prev(Context context) {
        if (tracks.isEmpty()) play(context);
        if (tracks.size() > 1) {
            current = (current - 1 + tracks.size()) % tracks.size();
            SharedPreferences prefs = context.getSharedPreferences("basketball", Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean("musicEnabled", true);
            if (enabled) {
                startTrack(context, current);
            } else {
                stop();
            }
        }
    }
} 