package com.example.robles_farma.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robles_farma.R;
import com.example.robles_farma.adapter.ChatMessagesAdapter;
import com.example.robles_farma.model.ChatMessage;
import com.example.robles_farma.sharedpreferences.LoginStorage;
import com.example.robles_farma.websocket.ChatWebSocketClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    private ChatWebSocketClient wsClient;
    private EditText etMessage;
    private ImageButton btnSend;
    private RecyclerView recyclerViewMessages;
    private ChatMessagesAdapter adapter;

    private String chatId;
    private String doctorId;
    private String doctorName;
    private String currentUserId;

    private final Set<String> shownMessageIds = new HashSet<>();
    private boolean isHistoryLoaded = false;
    private boolean isViewCreated = false;

    private static final String KEY_CHAT_ID = "chatId";
    private static final String KEY_DOCTOR_ID = "doctorId";
    private static final String KEY_DOCTOR_NAME = "doctorName";

    public ChatsFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        TextView chatHeader = view.findViewById(R.id.tvChatHeader);

        adapter = new ChatMessagesAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(adapter);

        currentUserId = String.valueOf(LoginStorage.getUserId(requireContext()));

        // Recuperar argumentos
        if (savedInstanceState != null) {
            chatId = savedInstanceState.getString(KEY_CHAT_ID);
            doctorId = savedInstanceState.getString(KEY_DOCTOR_ID);
            doctorName = savedInstanceState.getString(KEY_DOCTOR_NAME);
        } else if (getArguments() != null) {
            chatId = getArguments().getString(KEY_CHAT_ID);
            doctorId = getArguments().getString(KEY_DOCTOR_ID);
            doctorName = getArguments().getString(KEY_DOCTOR_NAME);
        }

        chatHeader.setText(doctorName != null ? doctorName : "Doctor");

        // Iniciar WebSocket
        if (!isViewCreated) {
            wsClient = new ChatWebSocketClient();
            wsClient.connect(requireContext());
            isViewCreated = true;
        }

        // Escuchar mensajes en tiempo real
        wsClient.getReceivedMessages().observe(getViewLifecycleOwner(), raw -> {
            try {
                JSONObject obj = new JSONObject(raw);

                String messageId = obj.optString("id", String.valueOf(System.currentTimeMillis()));
                String text = obj.optString("text", "");
                String timestamp = obj.optString("timestamp", getCurrentTimestamp());
                String senderRol = obj.optString("sender_rol", "");
                String senderId = obj.optString("sender_id", "").trim(); // Trim para evitar espacios fantasma
                String type = obj.optString("type", "text");

                if (shownMessageIds.contains(messageId)) return;
                shownMessageIds.add(messageId);

                // LOG PARA DEPURAR ALINEACIÓN
                Log.d(TAG, "Procesando mensaje - SenderID: '" + senderId + "' vs CurrentUserID: '" + currentUserId + "'");

                boolean isSentByMe = senderId.equals(currentUserId.trim());
                String senderName = isSentByMe ? "Tú" :
                        (doctorName != null ? doctorName : "Doctor");

                ChatMessage message = new ChatMessage(
                        messageId, text, senderId, senderName, timestamp, isSentByMe
                );
                
                if ("location".equals(type)) {
                    JSONObject loc = obj.optJSONObject("location");
                    if (loc != null) {
                        message.setType("location");
                        message.setLatitude(loc.optDouble("latitude"));
                        message.setLongitude(loc.optDouble("longitude"));
                        message.setText("📍 Ubicación compartida");
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    adapter.addMessage(message);
                    recyclerViewMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                });

            } catch (JSONException e) {
                Log.e(TAG, "Error al procesar mensaje en tiempo real: " + e.getMessage());
            }
        });

        // Enviar mensaje
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                wsClient.sendMessage(message, chatId, Arrays.asList(currentUserId, doctorId));
                etMessage.setText("");
                Log.i(TAG, "Mensaje enviado → Chat: " + chatId);
            }
        });

        // Botón de ubicación
        ImageButton btnLocation = view.findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(v -> shareLocation());
    }

    private void shareLocation() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(requireContext(), 
                android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = 
                com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity());

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                wsClient.sendLocation(chatId, location.getLatitude(), location.getLongitude(), Arrays.asList(currentUserId, doctorId));
                android.widget.Toast.makeText(getContext(), "Ubicación enviada 📍", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(getContext(), "No se pudo obtener la ubicación", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            shareLocation();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CHAT_ID, chatId);
        outState.putString(KEY_DOCTOR_ID, doctorId);
        outState.putString(KEY_DOCTOR_NAME, doctorName);
    }

    private void loadChatHistory() {
        wsClient.loadChatHistory(requireContext(), chatId, json -> {
            try {
                JSONArray messagesArray = new JSONArray(json);
                List<ChatMessage> historyMessages = new ArrayList<>();

                requireActivity().runOnUiThread(() -> {
                    shownMessageIds.clear();
                    adapter.setMessages(new ArrayList<>());

                    for (int i = 0; i < messagesArray.length(); i++) {
                        JSONObject msg = messagesArray.optJSONObject(i);
                        if (msg == null) continue;

                        String messageId = msg.optString("id", String.valueOf(i));
                        String text = msg.optString("text", "");
                        String timestamp = msg.optString("timestamp", "");
                        String senderRol = msg.optString("sender_rol", "");
                        String senderId = msg.optString("sender_id", "").trim();
                        String type = msg.optString("type", "text");

                        boolean isSentByMe = senderId.equals(currentUserId.trim());
                        String senderName = isSentByMe ? "Tú" :
                                (doctorName != null ? doctorName : "Doctor");

                        ChatMessage chatMsg = new ChatMessage(
                                messageId, text, senderId, senderName, timestamp, isSentByMe
                        );

                        if ("location".equals(type)) {
                            JSONObject loc = msg.optJSONObject("location");
                            if (loc != null) {
                                chatMsg.setType("location");
                                chatMsg.setLatitude(loc.optDouble("latitude"));
                                chatMsg.setLongitude(loc.optDouble("longitude"));
                                chatMsg.setText("📍 Ubicación compartida");
                            }
                        }
                        
                        historyMessages.add(chatMsg);
                    }

                    adapter.setMessages(historyMessages);
                    recyclerViewMessages.scrollToPosition(adapter.getItemCount() - 1);
                    isHistoryLoaded = true;

                    Log.i(TAG, "Historial cargado: " + messagesArray.length() + " mensajes");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error al cargar historial: " + e.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chatId != null && !chatId.isEmpty() && !isHistoryLoaded) {
            loadChatHistory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (wsClient != null) {
            wsClient.getReceivedMessages().removeObservers(getViewLifecycleOwner());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shownMessageIds.clear();
        isHistoryLoaded = false;
        isViewCreated = false;
        if (wsClient != null) {
            wsClient.disconnect();
        }
        Log.i(TAG, "Chat cerrado correctamente 🧹");
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}
