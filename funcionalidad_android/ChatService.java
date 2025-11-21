package com.example.robles_farma.retrofit;

import com.example.robles_farma.request.ChatCreateRequest;
import com.example.robles_farma.response.ChatResponse;
import com.example.robles_farma.response.MessageResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatService {

    @POST("chats/")
    Call<ChatResponse> createOrGetChat(@Body ChatCreateRequest request);

    @GET("chats/paciente")
    Call<List<com.example.robles_farma.response.ChatResponse>> getChats();

    @GET("chats/{chatId}/messages/")
    Call<List<MessageResponse>> getMessages(@Path("chatId") String chatId);

    @GET("chats/paciente/{chatId}/messages/")
    Call<List<MessageResponse>> getChatMessages(@Path("chatId") String chatId);

}
