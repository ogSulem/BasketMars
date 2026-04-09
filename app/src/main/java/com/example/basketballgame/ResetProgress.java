package com.example.basketballgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Временный класс для сброса прогресса игры.
 * Используйте этот класс один раз, затем можете его удалить.
 */
public class ResetProgress {
    
    /**
     * Сбрасывает весь прогресс игры к значениям по умолчанию.
     * @param context контекст приложения
     * @return true если сброс успешен
     */
    public static boolean resetAllProgress(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("basketball", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // Сбрасываем все значения к начальным
            editor.putInt("selectedBall", 0);
            editor.putInt("selectedHoop", 0);
            editor.putInt("unlockedBall", 0);
            editor.putInt("unlockedHoop", 0);
            editor.putInt("achievementLevel", 0);
            editor.putBoolean("achievementUnlocked", false);
            editor.putInt("unlockedBg", 0);
            editor.putInt("selectedBg", 0);
            
            // Сохраняем настройки музыки, если хотите их не сбрасывать
            // boolean musicEnabled = prefs.getBoolean("musicEnabled", true);
            // editor.putBoolean("musicEnabled", musicEnabled);
            
            // Или сбрасываем и музыку
            editor.putBoolean("musicEnabled", true);
            editor.putBoolean("vibrationEnabled", true);
            
            // Применяем изменения
            boolean result = editor.commit();
            
            if (result) {
                    Toast.makeText(context, context.getString(R.string.progress_reset_ok), Toast.LENGTH_LONG).show();
                    Log.i("ResetProgress", "Progress reset successfully");
                } else {
                    Toast.makeText(context, context.getString(R.string.progress_reset_error), Toast.LENGTH_LONG).show();
                    Log.e("ResetProgress", "Failed to reset progress");
                }
            
            return result;
        } catch (Exception e) {
            Log.e("ResetProgress", "Exception resetting progress", e);
            Toast.makeText(context, context.getString(R.string.progress_reset_error), Toast.LENGTH_LONG).show();
            return false;
        }
    }
} 