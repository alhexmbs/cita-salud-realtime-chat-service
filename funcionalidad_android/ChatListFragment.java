package com.example.robles_farma.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robles_farma.R;
import com.example.robles_farma.adapter.ChatListAdapter;
import com.example.robles_farma.response.ChatResponse;
import com.example.robles_farma.response.MessageResponse;
import com.example.robles_farma.retrofit.ChatService;
import com.example.robles_farma.retrofit.RetrofitClient;
import com.example.robles_farma.sharedpreferences.LoginStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment {

    public static Map<String, String> doctorNames = new HashMap<>();
    private static final String TAG = "ChatListFragment";
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private final List<ChatItem> chatList = new ArrayList<>();
    private final Map<String, ChatItem> chatMap = new HashMap<>();
    private boolean isLoading = false;

    public ChatListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvChatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChatListAdapter(chatList, chat -> {
            Bundle bundle = new Bundle();
            bundle.putString("chatId", chat.getId());
            bundle.putString("doctorName", chat.getName());
            bundle.putString("doctorId", chat.getDoctorId());

            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_navigation_chat_to_chat_detail, bundle);
        });

        recyclerView.setAdapter(adapter);

        if (!isLoading && chatList.isEmpty()) {
            loadChats();
        }
    }

    private void loadChats() {
        if (isLoading) return;

        String token = LoginStorage.getToken(requireContext());
        String userId = LoginStorage.getUserId(requireContext());

        if (token == null) {
            Toast.makeText(requireContext(), "Sesión expirada. Inicia sesión.", Toast.LENGTH_LONG).show();
            return;
        }

        RetrofitClient.API_TOKEN = token;
        isLoading = true;

        ChatService chatService = RetrofitClient.createService(requireContext(), ChatService.class);

        chatService.getChats().enqueue(new Callback<List<ChatResponse>>() {
            @Override
            public void onResponse(Call<List<ChatResponse>> call, Response<List<ChatResponse>> response) {
                isLoading = false;

                if (!response.isSuccessful() || response.body() == null) return;

                chatList.clear();
                chatMap.clear();

                for (ChatResponse chat : response.body()) {
                    String chatId = chat.getChatId();

                    // Buscar el otro participante
                    ChatResponse.Participant other = null;

                    for (ChatResponse.Participant p : chat.getParticipants()) {
                        if (!p.getUserId().equals(userId)) {
                            other = p;
                            break;
                        }
                    }

                    if (other == null) continue;

                    // Obtener nombre real enviado por backend
                    String displayName = ChatListFragment.doctorNames.get(other.getUserId());
                    if (displayName == null || displayName.trim().isEmpty()) {
                        displayName = "Sin nombre";
                    }

                    ChatItem item = new ChatItem(
                            chatId,
                            displayName,             // <-- nombre REAL
                            "Cargando...",
                            "",
                            false,
                            other.getUserId()        // doctorId / pacienteId
                    );

                    chatMap.put(chatId, item);

                    // Cargar último mensaje
                    loadLastMessage(chatId);
                }

                refreshList();
            }

            @Override
            public void onFailure(Call<List<ChatResponse>> call, Throwable t) {
                isLoading = false;
                Toast.makeText(requireContext(), "Error al cargar chats", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadLastMessage(String chatId) {
        ChatService chatService = RetrofitClient.createService(requireContext(), ChatService.class);

        chatService.getChatMessages(chatId).enqueue(new Callback<List<MessageResponse>>() {
            @Override
            public void onResponse(Call<List<MessageResponse>> call, Response<List<MessageResponse>> response) {
                ChatItem item = chatMap.get(chatId);
                if (item == null) return;

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    MessageResponse last = response.body().get(response.body().size() - 1);
                    item.setLastMessage(last.getText());
                    item.setTimestamp(last.getTimestamp());
                } else {
                    item.setLastMessage("Sin mensajes aún 💬");
                    item.setTimestamp("");
                }

                refreshList();
            }

            @Override
            public void onFailure(Call<List<MessageResponse>> call, Throwable t) {
                Log.e(TAG, "Error cargando mensajes: " + t.getMessage());
            }
        });
    }

    private void refreshList() {
        requireActivity().runOnUiThread(() -> {
            chatList.clear();
            chatList.addAll(chatMap.values());
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoading && !chatList.isEmpty()) {
            chatList.clear();
            chatMap.clear();
            adapter.notifyDataSetChanged();
            loadChats();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatMap.clear();
        isLoading = false;
    }
}
