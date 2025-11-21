package com.example.robles_farma.retrofit;

import com.example.robles_farma.response.ItemResponse;
import com.example.robles_farma.response.PersonalMedicoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PersonalMedicoService {

    @GET("personal_medico/{id}")
    Call<ItemResponse<PersonalMedicoResponse>> getPersonalMedicoById(@Path("id") String id);
}
