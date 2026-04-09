package com.example.basketballgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Вспомогательный класс для переключения языка интерфейса.
 *
 * <p>Язык хранится в SharedPreferences по ключу {@code "appLang"}.
 * Применяется при старте каждой Activity через {@link #wrap(Context)}.</p>
 *
 * <p>Пример использования в Activity:
 * <pre>
 *   {@literal @}Override
 *   protected void attachBaseContext(Context base) {
 *       super.attachBaseContext(LocaleHelper.wrap(base));
 *   }
 * </pre>
 * </p>
 */
public class LocaleHelper {

    public static final String LANG_RU = "ru";
    public static final String LANG_EN = "en";
    private static final String PREF_KEY  = "appLang";
    private static final String PREFS_NAME = "basketball";

    /** Возвращает сохранённый язык (по умолчанию "ru"). */
    public static String getSavedLang(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_KEY, LANG_RU);
    }

    /** Сохраняет выбранный язык. */
    public static void saveLang(Context ctx, String lang) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(PREF_KEY, lang).apply();
    }

    /**
     * Оборачивает Context с нужной локалью.
     * Вызывается из {@code attachBaseContext} каждой Activity.
     */
    public static Context wrap(Context ctx) {
        String lang = getSavedLang(ctx);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(ctx.getResources().getConfiguration());
        config.setLocale(locale);
        return ctx.createConfigurationContext(config);
    }
}
