package com.example.robles_farma.websocket;

import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ChatWebSocketListener extends WebSocketListener {
    private static final String TAG = "ChatWebSocketListener";

    // 👂 Interfaz para comunicar mensajes al Fragment
    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    private final OnMessageReceivedListener listener;

    public ChatWebSocketListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(TAG, "WebSocket conectado correctamente ✅");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // Enviar el mensaje al Fragment
        if (listener != null) listener.onMessageReceived(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        // Mensaje binario recibido — opcional para debug
        Log.d(TAG, "Mensaje binario recibido (" + bytes.size() + " bytes)");
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        Log.i(TAG, "WebSocket cerrándose: " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "Error en WebSocket: " + t.getMessage());
    }
}
