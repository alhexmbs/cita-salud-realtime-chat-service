package com.example.robles_farma.websocket;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.robles_farma.sharedpreferences.LoginStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

public class ChatWebSocketClient {
    private static final String TAG = "ChatWebSocket";
    private WebSocket webSocket;
    private final OkHttpClient client = new OkHttpClient();
    private final MutableLiveData<String> receivedMessages = new MutableLiveData<>();
    private ChatWebSocketListener.OnMessageReceivedListener messageListener;

    public LiveData<String> getReceivedMessages() {
        return receivedMessages;
    }

    public void setMessageListener(ChatWebSocketListener.OnMessageReceivedListener listener) {
        this.messageListener = listener;
    }

    //  Conectar al WebSocket con token JWT
    public void connect(Context context) {
        try {
            String jwtToken = LoginStorage.getToken(context);
            if (jwtToken == null || jwtToken.isEmpty()) {
                Log.e(TAG, "Token no encontrado. No se puede establecer conexión WebSocket.");
                return;
            }

            String wsUrl = "wss://codestar.space/ws?token=" + jwtToken;

            Request request = new Request.Builder()
                    .url(wsUrl)
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .build();

            ChatWebSocketListener listener = new ChatWebSocketListener(message -> {
                receivedMessages.postValue(message);
                if (messageListener != null) messageListener.onMessageReceived(message);
            });

            webSocket = client.newWebSocket(request, listener);
            Log.i(TAG, "Conectado a WebSocket");
        } catch (Exception e) {
            Log.e(TAG, "Error al conectar WebSocket: " + e.getMessage());
        }
    }

    //  Enviar mensaje de texto
    public void sendMessage(String text, String chatId, List<String> recipientIds) {
        if (webSocket == null) {
            Log.e(TAG, "No hay conexión WebSocket activa. No se pudo enviar el mensaje.");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("type", "text"); // 👈 Importante para el backend
            json.put("text", text);
            json.put("chat_id", chatId);
            json.put("recipient_ids", new JSONArray(recipientIds));

            webSocket.send(json.toString());
            Log.i(TAG, "Mensaje enviado al chat: " + chatId);
        } catch (Exception e) {
            Log.e(TAG, "Error al crear/enviar mensaje: " + e.getMessage());
        }
    }

    //  Enviar ubicación
    public void sendLocation(String chatId, double lat, double lng, List<String> recipientIds) {
        if (webSocket == null) {
            Log.e(TAG, "No hay conexión WebSocket activa.");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("type", "location");
            json.put("chat_id", chatId);
            json.put("recipient_ids", new JSONArray(recipientIds));
            
            JSONObject location = new JSONObject();
            location.put("latitude", lat);
            location.put("longitude", lng);
            json.put("location", location);

            webSocket.send(json.toString());
            Log.i(TAG, "Ubicación enviada al chat: " + chatId);
        } catch (Exception e) {
            Log.e(TAG, "Error al enviar ubicación: " + e.getMessage());
        }
    }

    //  Cargar historial de mensajes
    public void loadChatHistory(Context context, String chatId, OnMessagesLoadedListener listener) {
        new Thread(() -> {
            try {
                String jwtToken = LoginStorage.getToken(context);
                if (jwtToken == null || jwtToken.isEmpty()) {
                    Log.e(TAG, "Token no encontrado. No se puede cargar historial.");
                    return;
                }

                String url = "https://citassalud-production.up.railway.app/chats/paciente/" + chatId + "/messages";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + jwtToken)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        listener.onMessagesLoaded(json);
                        Log.i(TAG, "Historial cargado correctamente para chatId: " + chatId);
                    } else {
                        Log.e(TAG, "Error HTTP al obtener historial. Código: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar historial: " + e.getMessage());
            }
        }).start();
    }

    //  Desconectar
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Desconectado por el usuario");
            webSocket = null;
            Log.i(TAG, "WebSocket desconectado correctamente 🔌");
        }
    }

    // 👂 Interfaz para callback del historial
    public interface OnMessagesLoadedListener {
        void onMessagesLoaded(String jsonResponse);
    }
}
