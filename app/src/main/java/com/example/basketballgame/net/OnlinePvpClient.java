package com.example.basketballgame.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
 *     player1Id:   "uid1"          player2Id:   "uid2"
 *     player1Name: "Марсель"       player2Name: "Tim"
 *     status:      "waiting" | "playing" | "finished"
 *     startTime:   epoch_ms
 *     score1:      0               score2:      0      ← счёт игроков
 *     bx1: 0.5  by1: 0.75  bm1: false              ← позиция мяча player1 (норм. 0-1)
 *     bx2: 0.5  by2: 0.75  bm2: false              ← позиция мяча player2 (норм. 0-1)
 * </pre>
 * </p>
 *
 * <p>Координаты мяча нормализованы в диапазон [0, 1] (x / ширина, y / высота),
 * что обеспечивает корректное отображение на экранах с разным разрешением.</p>
 */
public class OnlinePvpClient implements MatchClient {

    private static final String TAG = "OnlinePvpClient";
    private static final String COLLECTION = "matches";

    /** Минимальный интервал между записями счёта в Firestore, мс. */
    private static final long SCORE_WRITE_INTERVAL_MS = 500;
    /** Интервал записи позиции мяча (~10 fps — баланс между плавностью и нагрузкой). */
    private static final long POS_WRITE_INTERVAL_MS = 100;

    private final String roomId;
    private final int myRole;           // 1 или 2
    private final String myScoreField;  // "score1" или "score2"
    private final String opScoreField;  // "score2" или "score1"
    // Поля позиции мяча (нормализованные 0-1): bx1/by1/bm1 или bx2/by2/bm2
    private final String myBxField, myByField, myBmField;
    private final String opBxField, opByField, opBmField;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Listener listener;
    private FirebaseFirestore db;
    private DocumentReference matchRef;
    private ListenerRegistration listenerRegistration;

    private boolean connected = false;
    private int lastWrittenScore = -1;
    private long lastScoreWriteMs = 0;
    private long lastPosWriteMs = 0;
    private boolean lastWrittenMoving = false;

    /**
     * @param roomId   идентификатор матча в Firestore
     * @param myRole   1 = player1, 2 = player2
     * @param listener колбэки к GameView
     */
    public OnlinePvpClient(String roomId, int myRole, Listener listener) {
        this.roomId = roomId;
        this.myRole = myRole;
        this.listener = listener;
        String s = String.valueOf(myRole);
        String op = myRole == 1 ? "2" : "1";
        this.myScoreField = "score" + s;
        this.opScoreField = "score" + op;
        this.myBxField = "bx" + s;
        this.myByField = "by" + s;
        this.myBmField = "bm" + s;
        this.opBxField = "bx" + op;
        this.opByField = "by" + op;
        this.opBmField = "bm" + op;
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

        // Слушаем документ матча: получаем позицию мяча и счёт соперника в реальном времени.
        listenerRegistration = matchRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.w(TAG, "Match listener error", error);
                return;
            }
            if (snapshot == null || !snapshot.exists()) return;

            // Читаем данные снапшота до handler.post, пока объект ещё валиден.
            final Long opScoreLong = snapshot.getLong(opScoreField);
            final Double opBx = snapshot.getDouble(opBxField);
            final Double opBy = snapshot.getDouble(opByField);
            final Boolean opBm = snapshot.getBoolean(opBmField);

            handler.post(() -> {
                if (!connected || listener == null) return;

                int rivalScore = opScoreLong != null ? opScoreLong.intValue() : 0;
                // Нормализованные координаты [0,1]; при отсутствии данных — нейтральная позиция
                float ghostX = opBx != null ? opBx.floatValue() : 0.5f;
                float ghostY = opBy != null ? opBy.floatValue() : 0.75f;
                boolean ghostMoving = Boolean.TRUE.equals(opBm);

                listener.onGhostSnapshot(ghostX, ghostY, ghostMoving, rivalScore);
            });
        });

        // Обновляем статус матча на "playing" (второй игрок может написать позже — это нормально)
        Map<String, Object> update = new HashMap<>();
        update.put("status", "playing");
        update.put("startTime", System.currentTimeMillis());
        matchRef.update(update)
                .addOnSuccessListener(v -> handler.post(() -> listener.onConnected()))
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Status update skipped (likely already set by other player)");
                    handler.post(() -> listener.onConnected());
                });
    }

    /**
     * Отправляет снапшот состояния мяча.
     * x, y — нормализованные координаты [0, 1] (ballX/width, ballY/height).
     */
    @Override
    public void sendSnapshot(float x, float y, boolean moving, int score) {
        if (!connected || matchRef == null) return;

        long now = System.currentTimeMillis();
        Map<String, Object> batch = new HashMap<>();

        // --- Позиция мяча: пишем когда мяч летит (~10 fps) ---
        if (moving && now - lastPosWriteMs >= POS_WRITE_INTERVAL_MS) {
            batch.put(myBxField, (double) x);
            batch.put(myByField, (double) y);
            batch.put(myBmField, true);
            lastPosWriteMs = now;
            lastWrittenMoving = true;
        } else if (!moving && lastWrittenMoving) {
            // Мяч остановился — одиночное обновление флага
            batch.put(myBmField, false);
            lastWrittenMoving = false;
        }

        // --- Счёт: пишем при изменении (с троттлингом) ---
        if (score != lastWrittenScore && now - lastScoreWriteMs >= SCORE_WRITE_INTERVAL_MS) {
            batch.put(myScoreField, score);
            lastWrittenScore = score;
            lastScoreWriteMs = now;
        }

        if (!batch.isEmpty()) {
            matchRef.update(batch)
                    .addOnFailureListener(e -> Log.w(TAG, "Snapshot write failed", e));
        }
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
            matchRef.update("status", "finished")
                    .addOnFailureListener(e -> Log.w(TAG, "Finish update failed", e));
        }

        handler.post(() -> {
            if (listener != null) listener.onDisconnected();
        });
    }
}
