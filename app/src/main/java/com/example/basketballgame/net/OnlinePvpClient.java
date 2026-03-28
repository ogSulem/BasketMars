package com.example.basketballgame.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

/**
 * Реальный онлайн-клиент для режима PvP через Firebase Firestore.
 *
 * <p>Структура документа матча в Firestore:
 * <pre>
 *   matches/{roomId}/
 *     player1Id:   "uid1"
 *     player1Name: "Марсель"
 *     player2Id:   "uid2"
 *     player2Name: "Tim"
 *     status:      "waiting" | "playing" | "finished"
 *     startTime:   epoch_ms
 *     score1:      0          ← счёт player1
 *     score2:      0          ← счёт player2
 * </pre>
 * </p>
 *
 * <p>Клиент знает roomId и myRole (1 или 2). В sendSnapshot он обновляет
 * своё поле score1/score2; в слушателе — читает поле соперника.</p>
 */
public class OnlinePvpClient implements MatchClient {

    private static final String TAG = "OnlinePvpClient";
    private static final String COLLECTION = "matches";

    /** Минимальный интервал между записями счёта в Firestore, мс. */
    private static final long SCORE_WRITE_INTERVAL_MS = 800;

    private final String roomId;
    private final int myRole;          // 1 или 2
    private final String myScoreField; // "score1" или "score2"
    private final String opScoreField; // "score2" или "score1"
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Listener listener;
    private FirebaseFirestore db;
    private DocumentReference matchRef;
    private ListenerRegistration listenerRegistration;

    private boolean connected = false;
    private int lastWrittenScore = -1;
    private long lastWriteMs = 0;

    /** Последняя позиция "призрака" — фиксированная центральная зона для онлайн-режима. */
    private static final float GHOST_X = 540f;
    private static final float GHOST_Y = 400f;

    /**
     * @param roomId   идентификатор матча в Firestore
     * @param myRole   1 = player1, 2 = player2
     * @param listener колбэки к GameView
     */
    public OnlinePvpClient(String roomId, int myRole, Listener listener) {
        this.roomId = roomId;
        this.myRole = myRole;
        this.listener = listener;
        this.myScoreField = myRole == 1 ? "score1" : "score2";
        this.opScoreField = myRole == 1 ? "score2" : "score1";
    }

    @Override
    public void connect(String target) {
        if (connected) return;
        try {
            db = FirebaseFirestore.getInstance();
            matchRef = db.collection(COLLECTION).document(roomId);
        } catch (Exception e) {
            Log.e(TAG, "Firestore unavailable", e);
            handler.post(() -> listener.onError("Firebase недоступен", e));
            return;
        }

        connected = true;

        // Слушаем документ матча для получения счёта соперника
        listenerRegistration = matchRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.w(TAG, "Match listener error", error);
                return;
            }
            if (snapshot == null || !snapshot.exists()) return;

            Long opScore = snapshot.getLong(opScoreField);
            int rivalScore = opScore != null ? opScore.intValue() : 0;

            handler.post(() -> {
                if (connected && listener != null) {
                    listener.onGhostSnapshot(GHOST_X, GHOST_Y, true, rivalScore);
                }
            });
        });

        // Обновляем статус матча на "playing", если оба подключились
        Map<String, Object> update = new HashMap<>();
        update.put("status", "playing");
        update.put("startTime", System.currentTimeMillis());
        matchRef.update(update)
                .addOnSuccessListener(v -> handler.post(() -> listener.onConnected()))
                .addOnFailureListener(e -> {
                    // Документ мог быть уже обновлён другим игроком — это нормально
                    Log.d(TAG, "Status update skipped (likely already set)");
                    handler.post(() -> listener.onConnected());
                });
    }

    @Override
    public void sendSnapshot(float x, float y, boolean moving, int score) {
        if (!connected || matchRef == null) return;
        if (score == lastWrittenScore) return;

        long now = System.currentTimeMillis();
        if (now - lastWriteMs < SCORE_WRITE_INTERVAL_MS) return;

        lastWrittenScore = score;
        lastWriteMs = now;

        matchRef.update(myScoreField, score)
                .addOnFailureListener(e -> Log.w(TAG, "Score write failed", e));
    }

    @Override
    public void disconnect() {
        if (!connected) return;
        connected = false;

        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }

        if (matchRef != null) {
            // Помечаем матч как завершённый
            matchRef.update("status", "finished")
                    .addOnFailureListener(e -> Log.w(TAG, "Finish update failed", e));
        }

        handler.post(() -> {
            if (listener != null) listener.onDisconnected();
        });
    }
}
