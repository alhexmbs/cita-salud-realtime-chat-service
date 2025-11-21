package com.example.robles_farma.retrofit;

import android.content.Context;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.robles_farma.MyApplication;
import com.example.robles_farma.sharedpreferences.LoginStorage;

public class RetrofitClient {
    public static final String URL_API_SERVICE = "https://citassalud-production.up.railway.app/";
    public static String API_TOKEN;

    private static class AuthInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json");

            Context appContext = MyApplication.getAppContext();
            if (appContext != null) {
                LoginStorage loginStorage = new LoginStorage(appContext);
                String token = loginStorage.getToken();

                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }
            }

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }

    }

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor());

    public static Retrofit API_SERVICE = new Retrofit.Builder()
            .baseUrl(URL_API_SERVICE)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService createService() {
        return API_SERVICE.create(ApiService.class);
    }

    public static <T> T createService(Context context, Class<T> serviceClass) {
        return API_SERVICE.create(serviceClass);
    }
}