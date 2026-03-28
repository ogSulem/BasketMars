package com.example.basketballgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Экран матчмейкинга для режима «Онлайн PvP».
 *
 * <p>Алгоритм:
 * <ol>
 *   <li>Записываем себя в {@code matchmaking_pool/{myId}} со статусом {@code "searching"}.</li>
 *   <li>Ищем другого игрока с тем же статусом.</li>
 *   <li>Первый нашедший создаёт документ матча в {@code matches/{roomId}} и помечает
 *       обоих игроков как {@code "matched"}.</li>
 *   <li>Второй игрок видит изменение своего документа в пуле и тоже переходит в игру.</li>
 *   <li>Если за 30 секунд соперник не найден — предлагаем сыграть против бота.</li>
 * </ol>
 * </p>
 */
public class MatchmakingActivity extends AppCompatActivity {

    private static final String TAG = "Matchmaking";
    private static final String POOL_COLLECTION = "matchmaking_pool";
    private static final String MATCHES_COLLECTION = "matches";
    private static final long TIMEOUT_MS = 30_000L;

    public static final String EXTRA_ROOM_ID = "EXTRA_ROOM_ID";
    public static final String EXTRA_MY_ROLE = "EXTRA_MY_ROLE";
    public static final String EXTRA_OPPONENT_NAME = "EXTRA_OPPONENT_NAME";

    private FirebaseFirestore db;
    private String myId;
    private String myName;
    private DocumentReference myPoolRef;
    private ListenerRegistration myPoolListener;
    private ListenerRegistration searchListener;

    private TextView statusText;
    private boolean matched = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable = this::onTimeout;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaking);

        statusText = findViewById(R.id.matchmaking_status);

        Button cancelBtn = findViewById(R.id.btn_cancel_matchmaking);
        cancelBtn.setOnClickListener(v -> {
            GameView.animateButton(v);
            cancelMatchmaking();
            finish();
        });

        SharedPreferences prefs = getSharedPreferences("basketball", MODE_PRIVATE);
        myName = prefs.getString("playerName", getString(R.string.default_player_name));

        // Получаем стабильный userId: сначала из Firebase Auth, иначе — из SharedPreferences
        AuthManager auth = AuthManager.getInstance(this);
        if (auth.isSignedIn() && auth.getUserId() != null) {
            myId = auth.getUserId();
        } else {
            myId = prefs.getString("anonymousId", null);
            if (myId == null) {
                myId = UUID.randomUUID().toString();
                prefs.edit().putString("anonymousId", myId).apply();
            }
        }

        // Проверяем доступность Firestore
        try {
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Firestore unavailable", e);
            showFirebaseError();
            return;
        }

        startSearching();
    }

    private void startSearching() {
        setStatus(getString(R.string.matchmaking_searching));

        myPoolRef = db.collection(POOL_COLLECTION).document(myId);

        Map<String, Object> myEntry = new HashMap<>();
        myEntry.put("userId", myId);
        myEntry.put("playerName", myName);
        myEntry.put("status", "searching");
        myEntry.put("roomId", null);
        myEntry.put("timestamp", System.currentTimeMillis());

        myPoolRef.set(myEntry)
                .addOnSuccessListener(v -> {
                    listenOwnEntry();
                    searchForOpponent();
                    handler.postDelayed(timeoutRunnable, TIMEOUT_MS);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to write pool entry", e);
                    showFirebaseError();
                });
    }

    /** Слушаем свой документ: если нас matched — идём в игру. */
    private void listenOwnEntry() {
        myPoolListener = myPoolRef.addSnapshotListener((snap, err) -> {
            if (matched || err != null || snap == null || !snap.exists()) return;
            String status = snap.getString("status");
            String roomId = snap.getString("roomId");
            if ("matched".equals(status) && roomId != null) {
                String opponentName = snap.getString("opponentName");
                enterGame(roomId, 2, opponentName != null ? opponentName : getString(R.string.default_player_name));
            }
        });
    }

    /** Ищем другого игрока в пуле и пытаемся создать матч. */
    private void searchForOpponent() {
        long cutoff = System.currentTimeMillis() - 60_000L; // игнорируем записи старше 1 мин
        searchListener = db.collection(POOL_COLLECTION)
                .whereEqualTo("status", "searching")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnap, err) -> {
                    if (matched || err != null || querySnap == null) return;
                    for (DocumentSnapshot doc : querySnap.getDocuments()) {
                        String userId = doc.getString("userId");
                        Long ts = doc.getLong("timestamp");
                        if (userId == null || userId.equals(myId)) continue;
                        if (ts != null && ts < cutoff) continue;

                        String opponentName = doc.getString("playerName");
                        tryCreateMatch(userId, opponentName != null ? opponentName
                                : getString(R.string.default_player_name));
                        break;
                    }
                });
    }

    /** Транзакцией создаём матч, чтобы не было двойного матча. */
    private void tryCreateMatch(String opponentId, String opponentName) {
        if (matched) return;

        DocumentReference opRef = db.collection(POOL_COLLECTION).document(opponentId);
        String roomId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        DocumentReference matchRef = db.collection(MATCHES_COLLECTION).document(roomId);

        db.runTransaction(tx -> {
            DocumentSnapshot opSnap = tx.get(opRef);
            String opStatus = opSnap.getString("status");
            if (!"searching".equals(opStatus)) return null; // соперник уже занят

            // Создаём матч
            Map<String, Object> matchData = new HashMap<>();
            matchData.put("player1Id", myId);
            matchData.put("player1Name", myName);
            matchData.put("player2Id", opponentId);
            matchData.put("player2Name", opponentName);
            matchData.put("status", "waiting");
            matchData.put("startTime", 0L);
            matchData.put("score1", 0);
            matchData.put("score2", 0);
            matchData.put("createdAt", System.currentTimeMillis());
            tx.set(matchRef, matchData);

            // Помечаем соперника как matched
            Map<String, Object> opUpdate = new HashMap<>();
            opUpdate.put("status", "matched");
            opUpdate.put("roomId", roomId);
            opUpdate.put("opponentName", myName);
            tx.update(opRef, opUpdate);

            // Помечаем себя как matched
            Map<String, Object> myUpdate = new HashMap<>();
            myUpdate.put("status", "matched");
            myUpdate.put("roomId", roomId);
            tx.update(myPoolRef, myUpdate);

            return roomId;
        }).addOnSuccessListener(result -> {
            if (result != null) {
                enterGame(roomId, 1, opponentName);
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Transaction failed (retry expected)", e));
    }

    private void enterGame(String roomId, int myRole, String opponentName) {
        if (matched) return;
        matched = true;

        handler.removeCallbacks(timeoutRunnable);
        stopListeners();

        setStatus(getString(R.string.matchmaking_found, opponentName));

        handler.postDelayed(() -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameMode.EXTRA_KEY, GameMode.ONLINE_PVP.name());
            intent.putExtra(EXTRA_ROOM_ID, roomId);
            intent.putExtra(EXTRA_MY_ROLE, myRole);
            intent.putExtra(EXTRA_OPPONENT_NAME, opponentName);
            startActivity(intent);
            finish();
        }, 1000);
    }

    private void onTimeout() {
        if (matched) return;
        cancelMatchmaking();
        runOnUiThread(() -> {
            Toast.makeText(this, getString(R.string.matchmaking_timeout), Toast.LENGTH_LONG).show();
            // Запускаем режим с ботом как запасной вариант
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameMode.EXTRA_KEY, GameMode.ONLINE_DUEL.name());
            startActivity(intent);
            finish();
        });
    }

    private void cancelMatchmaking() {
        handler.removeCallbacks(timeoutRunnable);
        stopListeners();
        if (myPoolRef != null && !matched) {
            myPoolRef.delete().addOnFailureListener(e -> Log.w(TAG, "Pool cleanup failed", e));
        }
    }

    private void stopListeners() {
        if (myPoolListener != null) { myPoolListener.remove(); myPoolListener = null; }
        if (searchListener != null) { searchListener.remove(); searchListener = null; }
    }

    private void setStatus(String text) {
        runOnUiThread(() -> { if (statusText != null) statusText.setText(text); });
    }

    private void showFirebaseError() {
        runOnUiThread(() -> {
            Toast.makeText(this, getString(R.string.matchmaking_firebase_unavailable), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameMode.EXTRA_KEY, GameMode.ONLINE_DUEL.name());
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!matched) cancelMatchmaking();
    }
}
