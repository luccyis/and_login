package com.example.loginpage.ui.login;

import com.example.loginpage.data.model.LoginResponse;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitAPI {

    @Headers({
            "Accept: */*",
            "Connection: keep-alive",
            "Content-Type: application/json;charset=utf8"
    })
    @POST("/user")
    Call<LoginResponse> openSignIn(@Body JsonObject request);


}
