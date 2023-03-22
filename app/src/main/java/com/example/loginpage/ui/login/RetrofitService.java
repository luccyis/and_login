package com.example.loginpage.ui.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private static Retrofit instance = null;
    private static Gson gson = new GsonBuilder().setLenient().create();
    private static RetrofitAPI retrofitAPI;

    private RetrofitService(){
    }
    public static Retrofit getInstance() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl("http://dev.rkiot.net:9090/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return instance;
    }
}
