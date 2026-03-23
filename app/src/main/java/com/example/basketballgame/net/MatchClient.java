package com.example.basketballgame.net;

import androidx.annotation.Nullable;

public interface MatchClient {
    interface Listener {
        void onConnected();
        void onDisconnected();
        void onError(String message, @Nullable Throwable throwable);
        void onGhostSnapshot(float x, float y, boolean moving, int remoteScore);
    }

    void connect(String target);

    void sendSnapshot(float x, float y, boolean moving, int score);

    void disconnect();
}
