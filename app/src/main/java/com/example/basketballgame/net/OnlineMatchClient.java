package com.example.basketballgame.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Минимальный WebSocket клиент для онлайн-матчей.
 *
 * <p>Пока реализован как каркас с колбэками, без конкретного серверного API.
 * В дальнейшем здесь можно:
 * - реализовать авторизацию
 * - сериализацию/десериализацию сообщений
 * - повторные подключения
 * </p>
 */
public class OnlineMatchClient implements MatchClient {

    private static final String TAG = "OnlineMatchClient";

    private final OkHttpClient httpClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MatchClient.Listener listener;
    private WebSocket webSocket;
    private boolean connected = false;

    public OnlineMatchClient(MatchClient.Listener listener) {
        this.listener = listener;
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket держит соединение
            .build();
    }

    /**
     * Подключиться к матчу.
     * @param wsUrl адрес WebSocket сервера
     */
    @Override
    public void connect(String wsUrl) {
        if (connected) return;
        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = httpClient.newWebSocket(request, new WebSocketListenerImpl());
    }

    /**
     * Отправить текущий снепшот состояния игрока.
     */
    @Override
    public void sendSnapshot(float x, float y, boolean moving, int score) {
        if (webSocket == null || !connected) return;
        String payload = String.format(
            java.util.Locale.US,
            "{\"type\":\"snapshot\",\"x\":%.2f,\"y\":%.2f,\"moving\":%b,\"score\":%d}",
            x, y, moving, score
        );
        webSocket.send(payload);
    }

    @Override
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "client_exit");
            webSocket = null;
        }
        connected = false;
    }

    private class WebSocketListenerImpl extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            connected = true;
            mainHandler.post(() -> listener.onConnected());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            connected = false;
            webSocket.close(code, reason);
            mainHandler.post(() -> listener.onDisconnected());
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            connected = false;
            Log.e(TAG, "WebSocket failure", t);
            mainHandler.post(() -> listener.onError("WebSocket error", t));
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            handleMessage(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            handleMessage(bytes.utf8());
        }
    }

    private void handleMessage(String json) {
        // Простейший парсер (без внешних зависимостей)
        // Ожидаемый формат: {"type":"snapshot","x":...,"y":...,"moving":true,"score":5}
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            String type = obj.optString("type", "");
            if ("snapshot".equals(type)) {
                float x = (float) obj.optDouble("x", 0);
                float y = (float) obj.optDouble("y", 0);
                boolean moving = obj.optBoolean("moving", false);
                int score = obj.optInt("score", 0);
                mainHandler.post(() -> listener.onGhostSnapshot(x, y, moving, score));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse message: " + json, e);
        }
    }
}
