package com.example.basketballgame.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Облачный лидерборд на основе Firebase Firestore.
 *
 * <p>Структура коллекции:
 * <pre>
 *   leaderboard_scores/
 *     {userId}_{mode}/          ← один документ на (игрок × режим)
 *       mode:        "ARCADE"
 *       score:       42
 *       playerName:  "Марсель"
 *       userId:      "abc123"
 *       timestamp:   1711234567890
 * </pre>
 * </p>
 *
 * <p>Для запроса «топ-20 по режиму» в Firestore Console необходимо создать
 * составной индекс: {@code mode (ASC) + score (DESC)}.</p>
 *
 * <p><b>Важно:</b> требует рабочего {@code google-services.json}.
 * Все операции обрабатывают ошибки gracefully — при отсутствии сети
 * данные не сохраняются в облако, но приложение не крашится.</p>
 */
public class CloudLeaderboardRepository {

    private static final String TAG = "CloudLeaderboard";
    private static final String COLLECTION = "leaderboard_scores";

    private final FirebaseFirestore db;
    private boolean available = false;

    public CloudLeaderboardRepository() {
        FirebaseFirestore firestore = null;
        try {
            firestore = FirebaseFirestore.getInstance();
            // Включаем постоянное кэширование для офлайн-поддержки
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            firestore.setFirestoreSettings(settings);
            available = true;
        } catch (Exception e) {
            Log.w(TAG, "Firestore unavailable (placeholder google-services.json?): " + e.getMessage());
        }
        this.db = firestore;
    }

    /** true если Firestore инициализирован (даже без сети — есть кэш). */
    public boolean isAvailable() {
        return available && db != null;
    }

    /**
     * Сохранить счёт игрока в облаке.
     * Обновляет документ только если новый счёт превышает сохранённый.
     *
     * @param mode       строка из {@code GameMode.name()}
     * @param score      счёт игрока
     * @param playerName отображаемое имя
     * @param userId     Firebase UID (не null)
     */
    public void saveScore(@NonNull String mode, int score,
                          @NonNull String playerName, @NonNull String userId) {
        if (!isAvailable()) return;

        String docId = userId + "_" + mode;
        DocumentReference ref = db.collection(COLLECTION).document(docId);

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snap = transaction.get(ref);
            long currentBest = snap.exists() ? snap.getLong("score") != null
                    ? snap.getLong("score") : 0L : 0L;

            if (score > currentBest) {
                Map<String, Object> data = new HashMap<>();
                data.put("mode", mode);
                data.put("score", score);
                data.put("playerName", playerName);
                data.put("userId", userId);
                data.put("timestamp", System.currentTimeMillis());
                transaction.set(ref, data);
            }
            return null;
        }).addOnFailureListener(e -> Log.w(TAG, "saveScore failed", e));
    }

    /**
     * Получить топ-N записей по режиму из облака.
     *
     * @param mode     строка из {@code GameMode.name()}
     * @param limit    максимальное количество записей
     * @param callback получает список (может быть пустым), вызывается на background-потоке
     */
    public void getTopScores(@NonNull String mode, int limit,
                             @Nullable LeaderboardRepository.ListCallback<List<LeaderboardEntry>> callback) {
        if (!isAvailable() || callback == null) {
            if (callback != null) callback.onResult(new ArrayList<>());
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo("mode", mode)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        LeaderboardEntry entry = new LeaderboardEntry();
                        entry.mode = doc.getString("mode");
                        entry.playerName = doc.getString("playerName");
                        Long s = doc.getLong("score");
                        entry.score = s != null ? s.intValue() : 0;
                        Long ts = doc.getLong("timestamp");
                        entry.timestamp = ts != null ? ts : 0L;
                        entries.add(entry);
                    }
                    callback.onResult(entries);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "getTopScores failed", e);
                    callback.onResult(new ArrayList<>());
                });
    }
}
